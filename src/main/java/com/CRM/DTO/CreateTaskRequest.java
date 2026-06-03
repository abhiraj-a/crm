package com.CRM.DTO;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;

import java.time.LocalDate;
import java.util.UUID;

@Builder
@Getter
public class CreateTaskRequest {
    private String title;
    private String description;
    private LocalDate deadline;

    // Optional IDs to link the task to a specific deal, lead, or user
    private UUID relatedLeadId;
    private UUID relatedDealId;
    private UUID assignedToUserId;
}
