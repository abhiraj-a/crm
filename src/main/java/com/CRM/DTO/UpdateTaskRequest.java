package com.CRM.DTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTaskRequest {

    private String title;
    private String description;
    private LocalDate deadline;

    // Allows reassigning the task to someone else or linking it to a different deal/lead
    private UUID assignedToUserId;
    private UUID relatedLeadId;
    private UUID relatedDealId;
}
