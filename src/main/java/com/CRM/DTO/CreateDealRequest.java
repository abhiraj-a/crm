package com.CRM.DTO;

import com.CRM.Entity.DealStage;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class CreateDealRequest {
    private String title;
    private Double value;
    private DealStage stage;
    private UUID leadId;
    private UUID assignedToUserId;
    private LocalDate expectedCloseDate;
    private UUID accountId;
}
