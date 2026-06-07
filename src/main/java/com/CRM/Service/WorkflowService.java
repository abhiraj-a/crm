package com.CRM.Service;

import com.CRM.DTO.CreateWorkflowRequest;
import com.CRM.DTO.WorkflowResponse;
import com.CRM.Entity.*;
import com.CRM.Repo.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class WorkflowService {

    private final WorkFlowRepo workFlowRepo;
    private final WorkFlowNodeRepo workFlowNodeRepo;
    private final WorkFlowEdgeRepo workFlowEdgeRepo;
    private final UserRepo userRepo;

    /**
     * Creates a workflow with all its nodes and edges in a single transaction.
     * Uses tempId mapping to resolve client-side node references to server-generated UUIDs.
     */
    @Transactional
    public WorkflowResponse createWorkflow(CreateWorkflowRequest request, String authifyerId) {
        User currentUser = userRepo.findByAuthifyerId(authifyerId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Create the workflow
        WorkFlow workflow = new WorkFlow();
        workflow.setName(request.getName());
        workflow.setDescription(request.getDescription());
        workflow.setTriggerType(request.getTriggerType());
        workflow.setActive(request.getActive() != null ? request.getActive() : false);
        workflow.setOrganization(currentUser.getOrganization());
        workflow.setCreatedAt(LocalDateTime.now());
        workflow = workFlowRepo.save(workflow);

        // Create nodes and build a tempId → real node mapping
        Map<String, WorkFlowNode> tempIdToNode = new HashMap<>();
        List<WorkFlowNode> savedNodes = new ArrayList<>();

        if (request.getNodes() != null) {
            for (CreateWorkflowRequest.WorkflowNodeDTO nodeDTO : request.getNodes()) {
                WorkFlowNode node = new WorkFlowNode();
                node.setWorkflow(workflow);
                node.setNodeType(nodeDTO.getNodeType());
                node.setConfiguration(nodeDTO.getConfiguration());
                node.setPositionX(nodeDTO.getPositionX());
                node.setPositionY(nodeDTO.getPositionY());
                node = workFlowNodeRepo.save(node);
                savedNodes.add(node);

                if (nodeDTO.getTempId() != null) {
                    tempIdToNode.put(nodeDTO.getTempId(), node);
                }
            }
        }

        // Create edges using the tempId mapping
        List<WorkFlowEdge> savedEdges = new ArrayList<>();
        if (request.getEdges() != null) {
            for (CreateWorkflowRequest.WorkflowEdgeDTO edgeDTO : request.getEdges()) {
                WorkFlowNode sourceNode = tempIdToNode.get(edgeDTO.getSourceTempId());
                WorkFlowNode targetNode = tempIdToNode.get(edgeDTO.getTargetTempId());

                if (sourceNode == null || targetNode == null) {
                    throw new RuntimeException("Invalid edge reference: source or target node tempId not found");
                }

                WorkFlowEdge edge = new WorkFlowEdge();
                edge.setWorkflow(workflow);
                edge.setSourceNode(sourceNode);
                edge.setTargetNode(targetNode);
                edge.setConditionLabel(edgeDTO.getConditionLabel());
                edge = workFlowEdgeRepo.save(edge);
                savedEdges.add(edge);
            }
        }

        return mapToResponse(workflow, savedNodes, savedEdges);
    }

    /**
     * Returns all workflows for the current user's organization.
     */
    public List<WorkflowResponse> getAllWorkflows(String authifyerId) {
        User currentUser = userRepo.findByAuthifyerId(authifyerId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<WorkFlow> workflows = workFlowRepo.findByOrganizationId(currentUser.getOrganization().getId());

        return workflows.stream().map(wf -> {
            List<WorkFlowNode> nodes = workFlowNodeRepo.findByWorkflowId(wf.getId());
            List<WorkFlowEdge> edges = workFlowEdgeRepo.findByWorkflowId(wf.getId());
            return mapToResponse(wf, nodes, edges);
        }).collect(Collectors.toList());
    }

    /**
     * Returns a single workflow by ID.
     */
    public WorkflowResponse getWorkflowById(UUID workflowId, String authifyerId) {
        User currentUser = userRepo.findByAuthifyerId(authifyerId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        WorkFlow workflow = workFlowRepo.findById(workflowId)
                .orElseThrow(() -> new RuntimeException("Workflow not found"));

        if (!workflow.getOrganization().getId().equals(currentUser.getOrganization().getId())) {
            throw new RuntimeException("Unauthorized access");
        }

        List<WorkFlowNode> nodes = workFlowNodeRepo.findByWorkflowId(workflowId);
        List<WorkFlowEdge> edges = workFlowEdgeRepo.findByWorkflowId(workflowId);

        return mapToResponse(workflow, nodes, edges);
    }

    /**
     * Toggles a workflow's active status.
     */
    @Transactional
    public WorkflowResponse toggleWorkflow(UUID workflowId, String authifyerId) {
        User currentUser = userRepo.findByAuthifyerId(authifyerId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        WorkFlow workflow = workFlowRepo.findById(workflowId)
                .orElseThrow(() -> new RuntimeException("Workflow not found"));

        if (!workflow.getOrganization().getId().equals(currentUser.getOrganization().getId())) {
            throw new RuntimeException("Unauthorized access");
        }

        workflow.setActive(!workflow.getActive());
        workFlowRepo.save(workflow);

        return getWorkflowById(workflowId, authifyerId);
    }

    /**
     * Deletes a workflow and all its nodes and edges.
     */
    @Transactional
    public void deleteWorkflow(UUID workflowId, String authifyerId) {
        User currentUser = userRepo.findByAuthifyerId(authifyerId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        WorkFlow workflow = workFlowRepo.findById(workflowId)
                .orElseThrow(() -> new RuntimeException("Workflow not found"));

        if (!workflow.getOrganization().getId().equals(currentUser.getOrganization().getId())) {
            throw new RuntimeException("Unauthorized access");
        }

        // Delete edges first (they reference nodes)
        List<WorkFlowEdge> edges = workFlowEdgeRepo.findByWorkflowId(workflowId);
        workFlowEdgeRepo.deleteAll(edges);

        // Delete nodes
        List<WorkFlowNode> nodes = workFlowNodeRepo.findByWorkflowId(workflowId);
        workFlowNodeRepo.deleteAll(nodes);

        // Delete workflow
        workFlowRepo.delete(workflow);

        log.info("Deleted workflow: {}", workflow.getName());
    }

    // ======================================================================
    // MAPPER
    // ======================================================================

    private WorkflowResponse mapToResponse(WorkFlow workflow, List<WorkFlowNode> nodes, List<WorkFlowEdge> edges) {
        List<WorkflowResponse.NodeResponse> nodeResponses = nodes.stream()
                .map(n -> WorkflowResponse.NodeResponse.builder()
                        .id(n.getId())
                        .nodeType(n.getNodeType())
                        .configuration(n.getConfiguration())
                        .positionX(n.getPositionX())
                        .positionY(n.getPositionY())
                        .build())
                .collect(Collectors.toList());

        List<WorkflowResponse.EdgeResponse> edgeResponses = edges.stream()
                .map(e -> WorkflowResponse.EdgeResponse.builder()
                        .id(e.getId())
                        .sourceNodeId(e.getSourceNode().getId())
                        .targetNodeId(e.getTargetNode().getId())
                        .conditionLabel(e.getConditionLabel())
                        .build())
                .collect(Collectors.toList());

        return WorkflowResponse.builder()
                .id(workflow.getId())
                .name(workflow.getName())
                .description(workflow.getDescription())
                .triggerType(workflow.getTriggerType())
                .active(workflow.getActive())
                .createdAt(workflow.getCreatedAt())
                .nodes(nodeResponses)
                .edges(edgeResponses)
                .build();
    }
}
