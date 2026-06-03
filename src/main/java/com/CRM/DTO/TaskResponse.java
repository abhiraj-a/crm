package com.CRM.DTO;

import com.CRM.Entity.TaskStatus;
import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public class TaskResponse {
    private UUID id;
    private String title;
    private String description;
    private TaskStatus status;
    private LocalDate deadline;
    private LocalDateTime createdAt;

    // Flattened User Data
    private UUID assignedToUserId;
    private String assignedToUserName;

    // Flattened Lead Data
    private UUID relatedLeadId;
    private String relatedLeadName;

    // Flattened Deal Data
    private UUID relatedDealId;
    private String relatedDealTitle;
}
