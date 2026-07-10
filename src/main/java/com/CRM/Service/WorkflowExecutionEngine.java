package com.CRM.Service;
import com.CRM.Event.DealUpdatedEvent;
import com.CRM.Entity.*;
import com.CRM.Event.LeadCreatedEvent;
import com.CRM.Event.TaskCompletedEvent;
import com.CRM.Repo.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class WorkflowExecutionEngine {

    private final WorkFlowRepo workFlowRepo;
    private final WorkFlowNodeRepo workFlowNodeRepo;
    private final WorkFlowEdgeRepo workFlowEdgeRepo;

    private final LeadRepo leadRepo;
    private final UserRepo userRepo;
    private final TaskRepo taskRepo;
    private final DealRepo dealRepo;
    private final NotificationRepo notificationRepo;
    private final WorkflowExecutionRepo workflowExecutionRepo;
    private final ApprovalRequestRepo approvalRequestRepo;
    private final ScheduledWorkflowActionRepo scheduledWorkflowActionRepo;
    private final EmailService emailService;
    private  ObjectMapper objectMapper;

    // ======================================================================
    // EVENT LISTENERS — Multi-Trigger Support
    // ======================================================================

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleLeadCreated(LeadCreatedEvent event) {
        Lead lead = event.getLead();
        log.info("Workflow Engine started for new lead: {}", lead.getName());

        List<WorkFlow> activeWorkflows = workFlowRepo.findByOrganizationIdAndTriggerTypeAndActiveTrue(
                lead.getOrganization().getId(), TriggerType.LEAD_CREATED);

        for (WorkFlow workflow : activeWorkflows) {
            executeWorkflow(workflow, "Lead", lead.getId(), lead.getOrganization());
        }
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleDealUpdated(DealUpdatedEvent event) {
        Deal deal = event.getDeal();
        log.info("Workflow Engine started for updated deal: {}", deal.getTitle());

        List<WorkFlow> activeWorkflows = workFlowRepo.findByOrganizationIdAndTriggerTypeAndActiveTrue(
                deal.getOrganization().getId(), TriggerType.DEAL_UPDATED);

        for (WorkFlow workflow : activeWorkflows) {
            executeWorkflow(workflow, "Deal", deal.getId(), deal.getOrganization());
        }
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleTaskCompleted(TaskCompletedEvent event) {
        Task task = event.getTask();
        log.info("Workflow Engine started for completed task: {}", task.getTitle());

        List<WorkFlow> activeWorkflows = workFlowRepo.findByOrganizationIdAndTriggerTypeAndActiveTrue(
                task.getOrganization().getId(), TriggerType.TASK_COMPLETED);

        for (WorkFlow workflow : activeWorkflows) {
            executeWorkflow(workflow, "Task", task.getId(), task.getOrganization());
        }
    }

    // ======================================================================
    // CORE EXECUTION — starts a new
    // workflow run
    // ======================================================================

    @Transactional
    public void executeWorkflow(WorkFlow workflow, String entityType, UUID entityId, Organization org) {
        // Create an execution record to track this run
        WorkflowExecution execution = WorkflowExecution.builder()
                .workflow(workflow)
                .entityType(entityType)
                .entityId(entityId)
                .status(WorkflowExecutionStatus.RUNNING)
                .organization(org)
                .startedAt(LocalDateTime.now())
                .build();
        execution = workflowExecutionRepo.save(execution);

        WorkFlowNode startNode = workFlowNodeRepo.findByWorkflowIdAndNodeType(workflow.getId(), NodeType.TRIGGER);
        if (startNode != null) {
            processNode(startNode, entityType, entityId, execution);
        } else {
            log.warn("Workflow {} has no TRIGGER node", workflow.getName());
            execution.setStatus(WorkflowExecutionStatus.FAILED);
            workflowExecutionRepo.save(execution);
        }
    }

    // ======================================================================
    // GRAPH TRAVERSAL — processes each node and follows edges
    // ======================================================================

    private void processNode(WorkFlowNode node, String entityType, UUID entityId, WorkflowExecution execution) {
        try {
            String routeLabel = null;

            switch (node.getNodeType()) {
                case TRIGGER:
                    // Just pass through to next nodes
                    break;

                case ACTION:
                    executeActionNode(node, entityType, entityId);
                    break;

                case CONDITION:
                    boolean result = evaluateConditionNode(node, entityType, entityId);
                    routeLabel = result ? "TRUE" : "FALSE";
                    break;

                case APPROVAL:
                    handleApprovalNode(node, entityType, entityId, execution);
                    return; // STOP — execution is paused, will resume on approval

                case DELAY:
                    handleDelayNode(node, entityType, entityId, execution);
                    return; // STOP — execution is paused, will resume on schedule
            }

            // Traverse to next nodes
            List<WorkFlowEdge> edges = workFlowEdgeRepo.findBySourceNodeId(node.getId());
            if (edges.isEmpty()) {
                // Terminal node — mark execution as completed
                execution.setStatus(WorkflowExecutionStatus.COMPLETED);
                execution.setCompletedAt(LocalDateTime.now());
                workflowExecutionRepo.save(execution);
            }
            for (WorkFlowEdge edge : edges) {
                if (routeLabel != null && !routeLabel.equalsIgnoreCase(edge.getConditionLabel())) {
                    continue;
                }
                processNode(edge.getTargetNode(), entityType, entityId, execution);
            }

        } catch (Exception e) {
            log.error("Failed to process workflow node {}: {}", node.getId(), e.getMessage());
            execution.setStatus(WorkflowExecutionStatus.FAILED);
            workflowExecutionRepo.save(execution);
        }
    }

    // ======================================================================
    // RESUME — called after approval or scheduled delay
    // ======================================================================

    @Transactional
    public void resumeFromNode(WorkFlowNode node, String entityType, UUID entityId, WorkflowExecution execution) {
        execution.setStatus(WorkflowExecutionStatus.RUNNING);
        execution.setPausedAtNode(null);
        workflowExecutionRepo.save(execution);

        // Continue traversal from the edges of the paused node
        List<WorkFlowEdge> edges = workFlowEdgeRepo.findBySourceNodeId(node.getId());
        for (WorkFlowEdge edge : edges) {
            processNode(edge.getTargetNode(), entityType, entityId, execution);
        }
    }

    // ======================================================================
    // APPROVAL NODE HANDLER
    // ======================================================================

    private void handleApprovalNode(WorkFlowNode node, String entityType, UUID entityId, WorkflowExecution execution) {
        try {
            String title = "Approval Required";
            String description = "Approval required for " + entityType;

            // Pause the execution
            execution.setStatus(WorkflowExecutionStatus.PAUSED_FOR_APPROVAL);
            execution.setPausedAtNode(node);
            workflowExecutionRepo.save(execution);

            log.info("Workflow paused for approval. Title: {}", title);

        } catch (Exception e) {
            log.error("Failed to handle approval node {}: {}", node.getId(), e.getMessage());
            execution.setStatus(WorkflowExecutionStatus.FAILED);
            workflowExecutionRepo.save(execution);
        }
    }

    // ======================================================================
    // DELAY NODE HANDLER
    // ======================================================================

    private void handleDelayNode(WorkFlowNode node, String entityType, UUID entityId, WorkflowExecution execution) {
        try {
            int delayMinutes = 0;
            int delayHours = 0;
            int delayDays = 0;
            int delayWeeks = 0;

            if (node.getConfiguration() != null && !node.getConfiguration().isEmpty()) {
                if (objectMapper == null) objectMapper = new ObjectMapper();
                JsonNode config = objectMapper.readTree(node.getConfiguration());
                
                if (config.has("waitType") && "FIXED_DURATION".equals(config.get("waitType").asText())) {
                    int duration = config.has("durationValue") ? config.get("durationValue").asInt() : 0;
                    String unit = config.has("durationUnit") ? config.get("durationUnit").asText() : "hours";
                    
                    switch (unit.toLowerCase()) {
                        case "minutes" -> delayMinutes = duration;
                        case "hours" -> delayHours = duration;
                        case "days" -> delayDays = duration;
                        case "weeks" -> delayWeeks = duration;
                    }
                }
            }

            LocalDateTime scheduledTime = LocalDateTime.now()
                    .plusMinutes(delayMinutes)
                    .plusHours(delayHours)
                    .plusDays(delayDays)
                    .plusWeeks(delayWeeks);

            // Pause the execution
            execution.setStatus(WorkflowExecutionStatus.PAUSED_FOR_DELAY);
            execution.setPausedAtNode(node);
            workflowExecutionRepo.save(execution);

            // Save a scheduled action for the poller to pick up
            ScheduledWorkflowAction scheduledAction = ScheduledWorkflowAction.builder()
                    .workflowExecution(execution)
                    .resumeFromNode(node)
                    .entityType(entityType)
                    .entityId(entityId)
                    .scheduledTime(scheduledTime)
                    .executed(false)
                    .organization(execution.getOrganization())
                    .createdAt(LocalDateTime.now())
                    .build();
            scheduledWorkflowActionRepo.save(scheduledAction);

            log.info("Workflow paused for delay. Scheduled to resume at: {}", scheduledTime);

        } catch (Exception e) {
            log.error("Failed to handle delay node {}: {}", node.getId(), e.getMessage());
            execution.setStatus(WorkflowExecutionStatus.FAILED);
            workflowExecutionRepo.save(execution);
        }
    }

    // ======================================================================
    // CONDITION EVALUATOR — supports dynamic Lead, Deal, and Task fields
    // ======================================================================

    private boolean evaluateConditionNode(WorkFlowNode node, String entityType, UUID entityId) throws Exception {
        if (node.getConfiguration() == null || node.getConfiguration().isEmpty()) {
            return true;
        }
        if (objectMapper == null) {
            objectMapper = new ObjectMapper();
        }
        JsonNode config = objectMapper.readTree(node.getConfiguration());
        
        String logic = config.has("logic") ? config.get("logic").asText().toUpperCase() : "AND";
        JsonNode conditionsNode = config.get("conditions");
        if (conditionsNode == null || !conditionsNode.isArray() || conditionsNode.isEmpty()) {
            return true;
        }

        Object entity = null;
        if ("Lead".equalsIgnoreCase(entityType)) {
            entity = leadRepo.findById(entityId).orElse(null);
        } else if ("Deal".equalsIgnoreCase(entityType)) {
            entity = dealRepo.findById(entityId).orElse(null);
        } else if ("Task".equalsIgnoreCase(entityType)) {
            entity = taskRepo.findById(entityId).orElse(null);
        }

        if (entity == null) return false;

        boolean isAnd = "AND".equals(logic);
        boolean finalResult = isAnd;

        for (JsonNode cond : conditionsNode) {
            if (!cond.has("field") || !cond.has("operator")) continue;
            String field = cond.get("field").asText();
            String operator = cond.get("operator").asText();
            String expectedValue = cond.has("value") ? cond.get("value").asText() : "";

            boolean result = evaluateSingleCondition(entity, field, operator, expectedValue);

            if (isAnd) {
                finalResult = finalResult && result;
                if (!finalResult) break; // short-circuit AND
            } else {
                finalResult = finalResult || result;
                if (finalResult) break; // short-circuit OR
            }
        }

        return finalResult;
    }

    private boolean evaluateSingleCondition(Object entity, String field, String operator, String expectedValue) {
        try {
            org.springframework.beans.BeanWrapper wrapper = org.springframework.beans.PropertyAccessorFactory.forBeanPropertyAccess(entity);
            if (!wrapper.isReadableProperty(field)) return false;
            
            Object actualValueObj = wrapper.getPropertyValue(field);
            if (actualValueObj == null) {
                return "is_empty".equalsIgnoreCase(operator);
            }

            if ("is_not_empty".equalsIgnoreCase(operator)) {
                return actualValueObj != null && !actualValueObj.toString().isEmpty();
            }

            if ("is_empty".equalsIgnoreCase(operator)) {
                return actualValueObj.toString().isEmpty();
            }

            String actualValue = actualValueObj.toString();

            return switch (operator.toLowerCase()) {
                case "equals" -> actualValue.equalsIgnoreCase(expectedValue);
                case "not_equals" -> !actualValue.equalsIgnoreCase(expectedValue);
                case "contains" -> actualValue.toLowerCase().contains(expectedValue.toLowerCase());
                case "greater_than" -> compareAsNumbers(actualValue, expectedValue) > 0;
                case "less_than" -> compareAsNumbers(actualValue, expectedValue) < 0;
                case "greater_than_or_equal" -> compareAsNumbers(actualValue, expectedValue) >= 0;
                case "less_than_or_equal" -> compareAsNumbers(actualValue, expectedValue) <= 0;
                default -> false;
            };
        } catch (Exception e) {
            log.error("Error evaluating condition field: {}", field, e);
            return false;
        }
    }

    private int compareAsNumbers(String actual, String expected) {
        try {
            double a = Double.parseDouble(actual);
            double b = Double.parseDouble(expected);
            return Double.compare(a, b);
        } catch (NumberFormatException e) {
            return actual.compareToIgnoreCase(expected); // Fallback to string comparison
        }
    }

    // ======================================================================
    // ACTION EXECUTOR — all automated actions
    // ======================================================================

    private void executeActionNode(WorkFlowNode node, String entityType, UUID entityId) throws Exception {
        log.info("Workflow Action node reached for {} {}", entityType, entityId);
        
        if (node.getConfiguration() == null || node.getConfiguration().isEmpty()) {
             log.warn("Action node {} has no configuration", node.getId());
             return;
        }

        if (objectMapper == null) {
            objectMapper = new ObjectMapper();
        }

        JsonNode config = objectMapper.readTree(node.getConfiguration());
        if (!config.has("actionType")) {
             log.warn("Action node {} has no actionType in configuration", node.getId());
             return;
        }

        String actionType = config.get("actionType").asText();

        switch (actionType) {
            case "SCHEDULE_TASK":
            case "CREATE_TASK":
                handleScheduleTask(config, entityType, entityId);
                break;
            case "SEND_EMAIL":
                handleSendEmail(config, entityType, entityId);
                break;
            case "AUTO_ASSIGN":
                handleAutoAssign(config, entityType, entityId);
                break;
            case "AUTO_UPDATE_STATUS":
                handleAutoUpdateStatus(config, entityType, entityId);
                break;
            case "CREATE_NOTIFICATION":
                handleCreateNotification(config, entityType, entityId);
                break;
            default:
                log.warn("Unknown actionType {} in Action node {}", actionType, node.getId());
        }
    }

    // --- AUTO_ASSIGN ---
    private void handleAutoAssign(JsonNode config, String entityType, UUID entityId) {
        if (!config.has("userId")) return;
        UUID targetUserId = UUID.fromString(config.get("userId").asText());

        userRepo.findById(targetUserId).ifPresent(user -> {
            if ("Lead".equals(entityType)) {
                leadRepo.findById(entityId).ifPresent(lead -> {
                    lead.setAssignedTo(user);
                    leadRepo.save(lead);
                    log.info("Workflow Action: Auto-Assigned lead {} to {}", lead.getName(), user.getFirstName());
                });
            } else if ("Deal".equals(entityType)) {
                dealRepo.findById(entityId).ifPresent(deal -> {
                    deal.setAssignedTo(user);
                    dealRepo.save(deal);
                    log.info("Workflow Action: Auto-Assigned deal {} to {}", deal.getTitle(), user.getFirstName());
                });
            } else if ("Task".equals(entityType)) {
                taskRepo.findById(entityId).ifPresent(task -> {
                    task.setAssignedTo(user);
                    taskRepo.save(task);
                    log.info("Workflow Action: Auto-Assigned task {} to {}", task.getTitle(), user.getFirstName());
                });
            }
        });
    }

    // --- AUTO_UPDATE_STATUS ---
    private void handleAutoUpdateStatus(JsonNode config, String entityType, UUID entityId) {
        if (!config.has("newStatus")) return;
        String statusString = config.get("newStatus").asText();

        if ("Lead".equals(entityType)) {
            leadRepo.findById(entityId).ifPresent(lead -> {
                lead.setStatus(LeadStatus.valueOf(statusString));
                leadRepo.save(lead);
                log.info("Workflow Action: Updated lead {} status to {}", lead.getName(), statusString);
            });
        } else if ("Deal".equals(entityType)) {
            dealRepo.findById(entityId).ifPresent(deal -> {
                deal.setStage(DealStage.valueOf(statusString));
                dealRepo.save(deal);
                log.info("Workflow Action: Updated deal {} stage to {}", deal.getTitle(), statusString);
            });
        } else if ("Task".equals(entityType)) {
            taskRepo.findById(entityId).ifPresent(task -> {
                task.setStatus(TaskStatus.valueOf(statusString));
                taskRepo.save(task);
                log.info("Workflow Action: Updated task {} status to {}", task.getTitle(), statusString);
            });
        }
    }

    // --- SEND_EMAIL (real implementation) ---
    private void handleSendEmail(JsonNode config, String entityType, UUID entityId) {
        String subjectTemplate = config.has("subject") ? config.get("subject").asText() : "CRM Notification";
        String bodyTemplate = config.has("body") ? config.get("body").asText() : "";
        String toTemplate = config.has("to") ? config.get("to").asText() : "";

        if (config.has("template")) {
            bodyTemplate = "Automated email triggered by workflow. " + bodyTemplate;
        }

        Object entity = null;
        if ("Lead".equals(entityType)) {
            entity = leadRepo.findById(entityId).orElse(null);
        } else if ("Deal".equals(entityType)) {
            entity = dealRepo.findById(entityId).orElse(null);
        } else if ("Task".equals(entityType)) {
            entity = taskRepo.findById(entityId).orElse(null);
        }

        if (entity != null) {
            String to = resolveVariables(toTemplate, entity);
            String subject = resolveVariables(subjectTemplate, entity);
            String body = resolveVariables(bodyTemplate, entity);

            if (to == null || to.isEmpty() || to.contains("{{")) {
                if (entity instanceof Lead lead) to = lead.getEmail();
            }

            if (to != null && !to.isEmpty() && !to.contains("{{")) {
                emailService.sendEmail(to, subject, body);
                log.info("Workflow Action: Sent email to {} with subject '{}'", to, subject);
            }
        }
    }

    // --- CREATE_NOTIFICATION ---
    private void handleCreateNotification(JsonNode config, String entityType, UUID entityId) {
        String titleTemplate = config.has("title") ? config.get("title").asText() : "CRM Alert";
        String messageTemplate = config.has("message") ? config.get("message").asText() : "";

        Object entity = null;
        User user = null;
        Organization org = null;

        if ("Lead".equals(entityType)) {
            Lead lead = leadRepo.findById(entityId).orElse(null);
            if (lead != null) { entity = lead; user = lead.getAssignedTo(); org = lead.getOrganization(); }
        } else if ("Deal".equals(entityType)) {
            Deal deal = dealRepo.findById(entityId).orElse(null);
            if (deal != null) { entity = deal; user = deal.getAssignedTo(); org = deal.getOrganization(); }
        } else if ("Task".equals(entityType)) {
            Task task = taskRepo.findById(entityId).orElse(null);
            if (task != null) { entity = task; user = task.getAssignedTo(); org = task.getOrganization(); }
        }

        if (user != null && org != null) {
            String title = resolveVariables(titleTemplate, entity);
            String message = resolveVariables(messageTemplate, entity);

            Notification notification = Notification.builder()
                    .title(title)
                    .message(message)
                    .isRead(false)
                    .user(user)
                    .organization(org)
                    .createdAt(LocalDateTime.now())
                    .build();
            notificationRepo.save(notification);
            log.info("Workflow Action: Created notification for user {}", user.getFirstName());
        }
    }

    // --- SCHEDULE_TASK ---
    private void handleScheduleTask(JsonNode config, String entityType, UUID entityId) {
        String titleTemplate = config.has("taskTitle") ? config.get("taskTitle").asText() : "Automated Follow-up";
        int daysDue = config.has("dueDays") ? config.get("dueDays").asInt() : 1;
        String descTemplate = config.has("description") ? config.get("description").asText() : "";

        Object entity = null;
        if ("Lead".equals(entityType)) {
            entity = leadRepo.findById(entityId).orElse(null);
        } else if ("Deal".equals(entityType)) {
            entity = dealRepo.findById(entityId).orElse(null);
        }

        if (entity != null) {
            String taskTitle = resolveVariables(titleTemplate, entity);
            String taskDescription = resolveVariables(descTemplate, entity);

            if ("Lead".equals(entityType)) {
                Lead lead = (Lead) entity;
                Task followUpTask = Task.builder()
                        .title(taskTitle)
                        .description(taskDescription.isEmpty()
                                ? "Automated task generated by workflow for lead: " + lead.getName()
                                : taskDescription)
                        .deadline(LocalDate.now().plusDays(daysDue))
                        .status(TaskStatus.PENDING)
                        .createdAt(LocalDateTime.now())
                        .organization(lead.getOrganization())
                        .assignedTo(lead.getAssignedTo())
                        .relatedLead(lead)
                        .build();

                taskRepo.save(followUpTask);
                log.info("Workflow Action: Scheduled task '{}' for lead {}", taskTitle, lead.getName());
            } else if ("Deal".equals(entityType)) {
                Deal deal = (Deal) entity;
                Task followUpTask = Task.builder()
                        .title(taskTitle)
                        .description(taskDescription.isEmpty()
                                ? "Automated task generated by workflow for deal: " + deal.getTitle()
                                : taskDescription)
                        .deadline(LocalDate.now().plusDays(daysDue))
                        .status(TaskStatus.PENDING)
                        .createdAt(LocalDateTime.now())
                        .organization(deal.getOrganization())
                        .assignedTo(deal.getAssignedTo())
                        .relatedDeal(deal)
                        .build();

                taskRepo.save(followUpTask);
                log.info("Workflow Action: Scheduled task '{}' for deal {}", taskTitle, deal.getTitle());
            }
        }
    }

    // ======================================================================
    // HELPERS
    // ======================================================================

    private String resolveVariables(String text, Object entity) {
        if (text == null || text.isEmpty()) return text;
        if (entity == null) return text;
        
        org.springframework.beans.BeanWrapper wrapper = org.springframework.beans.PropertyAccessorFactory.forBeanPropertyAccess(entity);
        
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\{\\{.*?\\.(.*?)\\}\\}");
        java.util.regex.Matcher matcher = pattern.matcher(text);
        
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String field = matcher.group(1);
            String replacement = "";
            try {
                if (wrapper.isReadableProperty(field)) {
                    Object val = wrapper.getPropertyValue(field);
                    replacement = val != null ? val.toString() : "";
                } else {
                    replacement = matcher.group(0); // keep original if not found
                }
            } catch (Exception e) {
                replacement = matcher.group(0);
            }
            matcher.appendReplacement(sb, java.util.regex.Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    private Deal findDealById(UUID dealId) {
        return dealRepo.findById(dealId).orElse(null);
    }
}
