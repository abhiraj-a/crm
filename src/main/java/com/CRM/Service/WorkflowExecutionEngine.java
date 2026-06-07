package com.CRM.Service;
import com.CRM.Event.DealUpdatedEvent;
import com.CRM.Entity.*;
import com.CRM.Event.LeadCreatedEvent;
import com.CRM.Event.TaskCompletedEvent;
import com.CRM.Repo.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
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
    private final ObjectMapper objectMapper;

    // ======================================================================
    // EVENT LISTENERS — Multi-Trigger Support
    // ======================================================================

    @Async
    @EventListener
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
    @EventListener
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
    @EventListener
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
            JsonNode config = objectMapper.readTree(node.getConfiguration());
            String approverId = config.has("approverId") ? config.get("approverId").asText() : null;
            String title = config.has("title") ? config.get("title").asText() : "Approval Required";
            String description = config.has("description") ? config.get("description").asText() : "";

            // Pause the execution
            execution.setStatus(WorkflowExecutionStatus.PAUSED_FOR_APPROVAL);
            execution.setPausedAtNode(node);
            workflowExecutionRepo.save(execution);

            // Find the approver
            User approver = null;
            if (approverId != null) {
                approver = userRepo.findById(UUID.fromString(approverId)).orElse(null);
            }

            if (approver == null) {
                log.error("Approval node {} has no valid approver configured", node.getId());
                execution.setStatus(WorkflowExecutionStatus.FAILED);
                workflowExecutionRepo.save(execution);
                return;
            }

            // Create the approval request
            ApprovalRequest request = ApprovalRequest.builder()
                    .workflowExecution(execution)
                    .approver(approver)
                    .status(ApprovalStatus.PENDING)
                    .requestTitle(title)
                    .requestDescription(description + " [Entity: " + entityType + " " + entityId + "]")
                    .organization(execution.getOrganization())
                    .createdAt(LocalDateTime.now())
                    .build();
            approvalRequestRepo.save(request);

            // Create an in-app notification for the approver
            Notification notification = Notification.builder()
                    .title("Approval Required: " + title)
                    .message(description)
                    .isRead(false)
                    .user(approver)
                    .organization(execution.getOrganization())
                    .createdAt(LocalDateTime.now())
                    .build();
            notificationRepo.save(notification);

            // Also send an email notification to the approver
            emailService.sendEmail(
                    approver.getEmail(),
                    "CRM Approval Required: " + title,
                    "You have a pending approval request.\n\n" +
                            "Title: " + title + "\n" +
                            "Description: " + description + "\n\n" +
                            "Please log in to your CRM dashboard to approve or reject."
            );

            log.info("Workflow paused for approval. Approver: {}, Title: {}", approver.getFirstName(), title);

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
            JsonNode config = objectMapper.readTree(node.getConfiguration());
            int delayHours = config.has("delayHours") ? config.get("delayHours").asInt() : 0;
            int delayDays = config.has("delayDays") ? config.get("delayDays").asInt() : 0;

            LocalDateTime scheduledTime = LocalDateTime.now()
                    .plusHours(delayHours)
                    .plusDays(delayDays);

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
    // CONDITION EVALUATOR — supports Lead, Deal, and Task fields
    // ======================================================================

    private boolean evaluateConditionNode(WorkFlowNode node, String entityType, UUID entityId) throws Exception {
        if (node.getConfiguration() == null || node.getConfiguration().isEmpty()) return false;

        JsonNode config = objectMapper.readTree(node.getConfiguration());
        String field = config.path("field").asText();
        String operator = config.path("operator").asText();
        String value = config.path("value").asText();

        if ("Lead".equals(entityType)) {
            Lead lead = leadRepo.findById(entityId).orElse(null);
            if (lead == null) return false;
            return evaluateLeadCondition(lead, field, operator, value);
        } else if ("Deal".equals(entityType)) {
            Deal deal = findDealById(entityId);
            if (deal == null) return false;
            return evaluateDealCondition(deal, field, operator, value);
        }

        return false;
    }

    private boolean evaluateLeadCondition(Lead lead, String field, String operator, String value) {
        if ("score".equalsIgnoreCase(field)) {
            int leadScore = lead.getScore() != null ? lead.getScore() : 0;
            int targetValue = Integer.parseInt(value);
            return compareNumbers(leadScore, targetValue, operator);
        } else if ("source".equalsIgnoreCase(field)) {
            String leadSource = lead.getSource() != null ? lead.getSource() : "";
            return "EQUALS".equalsIgnoreCase(operator) && leadSource.equalsIgnoreCase(value);
        } else if ("status".equalsIgnoreCase(field)) {
            return "EQUALS".equalsIgnoreCase(operator) && lead.getStatus().name().equalsIgnoreCase(value);
        }
        return false;
    }

    private boolean evaluateDealCondition(Deal deal, String field, String operator, String value) {
        if ("value".equalsIgnoreCase(field)) {
            double dealValue = deal.getValue() != null ? deal.getValue() : 0;
            double targetValue = Double.parseDouble(value);
            return compareNumbers((int) dealValue, (int) targetValue, operator);
        } else if ("stage".equalsIgnoreCase(field)) {
            return "EQUALS".equalsIgnoreCase(operator) && deal.getStage().name().equalsIgnoreCase(value);
        }
        return false;
    }

    private boolean compareNumbers(int actual, int target, String operator) {
        return switch (operator.toUpperCase()) {
            case "GREATER_THAN" -> actual > target;
            case "LESS_THAN" -> actual < target;
            case "EQUALS" -> actual == target;
            case "GREATER_THAN_OR_EQUAL" -> actual >= target;
            case "LESS_THAN_OR_EQUAL" -> actual <= target;
            default -> false;
        };
    }

    // ======================================================================
    // ACTION EXECUTOR — all automated actions
    // ======================================================================

    private void executeActionNode(WorkFlowNode node, String entityType, UUID entityId) throws Exception {
        if (node.getConfiguration() == null || node.getConfiguration().isEmpty()) return;

        JsonNode config = objectMapper.readTree(node.getConfiguration());
        String actionType = config.has("actionType") ? config.get("actionType").asText() : "UNKNOWN";

        switch (actionType) {
            case "AUTO_ASSIGN" -> handleAutoAssign(config, entityType, entityId);
            case "AUTO_UPDATE_STATUS" -> handleAutoUpdateStatus(config, entityType, entityId);
            case "SEND_EMAIL" -> handleSendEmail(config, entityType, entityId);
            case "CREATE_NOTIFICATION" -> handleCreateNotification(config, entityType, entityId);
            case "SCHEDULE_TASK" -> handleScheduleTask(config, entityType, entityId);
            default -> log.warn("Workflow Engine encountered an unknown action type: {}", actionType);
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
        String subject = config.has("subject") ? config.get("subject").asText() : "CRM Notification";
        String body = config.has("body") ? config.get("body").asText() : "";

        if ("Lead".equals(entityType)) {
            leadRepo.findById(entityId).ifPresent(lead -> {
                if (lead.getEmail() != null && !lead.getEmail().isEmpty()) {
                    // Replace template variables
                    String resolvedBody = body
                            .replace("{{name}}", lead.getName() != null ? lead.getName() : "")
                            .replace("{{company}}", lead.getCompany() != null ? lead.getCompany() : "")
                            .replace("{{email}}", lead.getEmail());

                    String resolvedSubject = subject
                            .replace("{{name}}", lead.getName() != null ? lead.getName() : "");

                    emailService.sendEmail(lead.getEmail(), resolvedSubject, resolvedBody);
                    log.info("Workflow Action: Sent email to {} with subject '{}'", lead.getEmail(), resolvedSubject);
                }
            });
        }
    }

    // --- CREATE_NOTIFICATION ---
    private void handleCreateNotification(JsonNode config, String entityType, UUID entityId) {
        String title = config.has("title") ? config.get("title").asText() : "CRM Alert";
        String message = config.has("message") ? config.get("message").asText() : "";

        if ("Lead".equals(entityType)) {
            leadRepo.findById(entityId).ifPresent(lead -> {
                if (lead.getAssignedTo() != null) {
                    Notification notification = Notification.builder()
                            .title(title)
                            .message(message + " — Lead: " + lead.getName())
                            .isRead(false)
                            .user(lead.getAssignedTo())
                            .organization(lead.getOrganization())
                            .createdAt(LocalDateTime.now())
                            .build();
                    notificationRepo.save(notification);
                    log.info("Workflow Action: Created notification for user {}", lead.getAssignedTo().getFirstName());
                }
            });
        } else if ("Deal".equals(entityType)) {
            dealRepo.findById(entityId).ifPresent(deal -> {
                if (deal.getAssignedTo() != null) {
                    Notification notification = Notification.builder()
                            .title(title)
                            .message(message + " — Deal: " + deal.getTitle())
                            .isRead(false)
                            .user(deal.getAssignedTo())
                            .organization(deal.getOrganization())
                            .createdAt(LocalDateTime.now())
                            .build();
                    notificationRepo.save(notification);
                    log.info("Workflow Action: Created notification for user {}", deal.getAssignedTo().getFirstName());
                }
            });
        } else if ("Task".equals(entityType)) {
            taskRepo.findById(entityId).ifPresent(task -> {
                if (task.getAssignedTo() != null) {
                    Notification notification = Notification.builder()
                            .title(title)
                            .message(message + " — Task: " + task.getTitle())
                            .isRead(false)
                            .user(task.getAssignedTo())
                            .organization(task.getOrganization())
                            .createdAt(LocalDateTime.now())
                            .build();
                    notificationRepo.save(notification);
                    log.info("Workflow Action: Created notification for user {}", task.getAssignedTo().getFirstName());
                }
            });
        }
    }

    // --- SCHEDULE_TASK ---
    private void handleScheduleTask(JsonNode config, String entityType, UUID entityId) {
        String taskTitle = config.has("title") ? config.get("title").asText() : "Automated Follow-up";
        int daysDue = config.has("daysDue") ? config.get("daysDue").asInt() : 1;
        String taskDescription = config.has("description") ? config.get("description").asText() : "";

        if ("Lead".equals(entityType)) {
            leadRepo.findById(entityId).ifPresent(lead -> {
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
            });
        } else if ("Deal".equals(entityType)) {
            dealRepo.findById(entityId).ifPresent(deal -> {
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
            });
        }
    }

    // ======================================================================
    // HELPERS
    // ======================================================================

    private Deal findDealById(UUID dealId) {
        return dealRepo.findById(dealId).orElse(null);
    }
}
