package com.CRM.DTO;

import jakarta.annotation.security.DenyAll;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateTaskRequest {
    private String title;
    private String description;
    private LocalDate deadline;

    // Optional IDs to link the task to a specific deal, lead, or user
    private UUID relatedLeadId;
    private UUID relatedDealId;
    private UUID assignedToUserId;
}
