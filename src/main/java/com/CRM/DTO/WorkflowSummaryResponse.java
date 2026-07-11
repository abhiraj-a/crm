package com.CRM.DTO;

import com.CRM.Entity.TriggerType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowSummaryResponse {
    private UUID id;
    private String name;
    private String description;
    private TriggerType triggerType;
    private Boolean active;
    private LocalDateTime createdAt;
    private Integer nodeCount;
}
