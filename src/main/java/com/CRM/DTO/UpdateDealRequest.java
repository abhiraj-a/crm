package com.CRM.DTO;

import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class UpdateDealRequest {
    private String title;
    private Double value;
    private UUID leadId;
    private UUID assignedToUserId;
    private LocalDate expectedCloseDate;
    private UUID accountId;
}
