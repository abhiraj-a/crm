package com.CRM.DTO;

import com.CRM.Entity.NodeType;
import com.CRM.Entity.TriggerType;
import lombok.Data;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

@Data
@Getter
public class CreateWorkflowRequest {
    private String name;
    private String description;
    private TriggerType triggerType;
    private Boolean active;
    private List<WorkflowNodeDTO> nodes;
    private List<WorkflowEdgeDTO> edges;

    @Data
    @Getter
    public static class WorkflowNodeDTO {
        private String tempId; // Temporary client-side ID for edge references
        private NodeType nodeType;
        private String configuration; // JSON string
        private Double positionX;
        private Double positionY;
    }

    @Data
    @Getter
    public static class WorkflowEdgeDTO {
        private String sourceTempId; // References a node's tempId
        private String targetTempId; // References a node's tempId
        private String conditionLabel; // "TRUE", "FALSE", or null
    }
}
