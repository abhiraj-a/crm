package com.CRM.DTO;

import com.CRM.Entity.DealStage;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class DealResponse {
    private UUID id;
    private String title;
    private Double value;
    private DealStage stage;
    private LocalDate expectedCloseDate;
    private LocalDateTime createdAt;

    // Flattened Lead info
    private UUID leadId;
    private String leadName;

    // Flattened User info
    private UUID assignedToUserId;
    private String assignedToUserName;

    // Flattened Account info
    private UUID accountId;
    private String accountName;
}
