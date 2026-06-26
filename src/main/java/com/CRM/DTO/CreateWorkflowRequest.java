package com.CRM.DTO;

import com.CRM.Entity.NodeType;
import com.CRM.Entity.TriggerType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

@Data
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateWorkflowRequest {
    private String name;
    private String description;
    private TriggerType triggerType;
    private Boolean active;
    private List<WorkflowNodeDTO> nodes;
    private List<WorkflowEdgeDTO> edges;

    @Data
    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class WorkflowNodeDTO {
        private String tempId; // Temporary client-side ID for edge references
        private NodeType nodeType;
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
