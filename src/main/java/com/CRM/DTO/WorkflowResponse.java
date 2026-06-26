package com.CRM.DTO;

import com.CRM.Entity.NodeType;
import com.CRM.Entity.TriggerType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.fasterxml.jackson.databind.JsonNode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowResponse {
    private UUID id;
    private String name;
    private String description;
    private TriggerType triggerType;
    private Boolean active;
    private LocalDateTime createdAt;
    private List<NodeResponse> nodes;
    private List<EdgeResponse> edges;

    @Data
    @Builder
    public static class NodeResponse {
        private UUID id;
        private NodeType nodeType;
        private Double positionX;
        private Double positionY;
        private JsonNode configuration;
    }

    @Data
    @Builder
    public static class EdgeResponse {
        private UUID id;
        private UUID sourceNodeId;
        private UUID targetNodeId;
        private String conditionLabel;
    }
}
