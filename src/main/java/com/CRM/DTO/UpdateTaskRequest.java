package com.CRM.DTO;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Builder
@Data
public class UpdateTaskRequest {

    private String title;
    private String description;
    private LocalDate deadline;

    // Allows reassigning the task to someone else or linking it to a different deal/lead
    private UUID assignedToUserId;
    private UUID relatedLeadId;
    private UUID relatedDealId;
}
