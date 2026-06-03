package com.CRM.Service;

import com.CRM.Entity.*;
import com.CRM.Event.LeadCreatedEvent;
import com.CRM.Repo.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
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
    private final ObjectMapper objectMapper; // Spring automatically provides this for JSON parsing

    // 1. Listen for the broadcast event in a background thread
    @Async
    @EventListener
    public void handleLeadCreated(LeadCreatedEvent event) {
        Lead lead = event.getLead();
        log.info("Workflow Engine started for new lead: {}", lead.getName());

        List<WorkFlow> activeWorkflows = workFlowRepo.findByOrganizationIdAndTriggerTypeAndActiveTrue(
                lead.getOrganization().getId(), TriggerType.LEAD_CREATED);

        for (WorkFlow workflow : activeWorkflows) {
            executeWorkflowForLead(workflow, lead);
        }
    }

    // 2. Find the starting point
    private void executeWorkflowForLead(WorkFlow workflow, Lead lead) {
        WorkFlowNode startNode = workFlowNodeRepo.findByWorkflowIdAndNodeType(workflow.getId(), NodeType.TRIGGER);
        if (startNode != null) {
            processNode(startNode, lead);
        }
    }

    // 3. Process current node and recursively move to the next connected nodes
    private void processNode(WorkFlowNode node, Lead lead) {
        try {
            if (node.getNodeType() == NodeType.ACTION) {
                executeActionNode(node, lead);
            }
            // Add Condition logic here later (e.g., IF lead.score > 50)

            // Traverse to the next nodes using the edges
            List<WorkFlowEdge> edges = workFlowEdgeRepo.findBySourceNodeId(node.getId());
            for (WorkFlowEdge edge : edges) {
                processNode(edge.getTargetNode(), lead);
            }

        } catch (Exception e) {
            log.error("Failed to process workflow node {}: {}", node.getId(), e.getMessage());
        }
    }

    // 4. Execute the specific JSON configuration
    private void executeActionNode(WorkFlowNode node, Lead lead) throws Exception {
        if (node.getConfiguration() == null || node.getConfiguration().isEmpty()) return;

        JsonNode config = objectMapper.readTree(node.getConfiguration());
        String actionType = config.has("actionType") ? config.get("actionType").asText() : "UNKNOWN";

        switch (actionType) {
            case "AUTO_ASSIGN":
                if (config.has("userId")) {
                    UUID targetUserId = UUID.fromString(config.get("userId").asText());
                    userRepo.findById(targetUserId).ifPresent(user -> {
                        lead.setAssignedTo(user);
                        leadRepo.save(lead);
                        log.info("Workflow Action: Auto-Assigned lead {} to {}", lead.getName(), user.getFirstName());
                    });
                }
                break;

            case "AUTO_UPDATE_STATUS":
                if (config.has("newStatus")) {
                    String statusString = config.get("newStatus").asText();
                    lead.setStatus(LeadStatus.valueOf(statusString));
                    leadRepo.save(lead);
                    log.info("Workflow Action: Updated lead {} status to {}", lead.getName(), statusString);
                }
                break;

            case "SEND_EMAIL":
                if (config.has("templateId") || config.has("subject")) {
                    String subject = config.has("subject") ? config.get("subject").asText() : "Welcome!";
                    // Here you would inject an EmailService and actually send it
                    // emailService.sendEmail(lead.getEmail(), subject, "Body content...");
                    log.info("Workflow Action: Mock Sent Email to {} with subject '{}'", lead.getEmail(), subject);
                }
                break;

            case "SCHEDULE_TASK":
                // You can inject TaskRepo here to automatically create a follow-up task
                log.info("Workflow Action: Scheduled task for lead {}", lead.getName());
                break;

            default:
                log.warn("Workflow Engine encountered an unknown action type: {}", actionType);
        }
    }
}
