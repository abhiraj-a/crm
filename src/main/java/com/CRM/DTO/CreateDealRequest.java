package com.CRM.DTO;

import com.CRM.Entity.DealStage;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data

@NoArgsConstructor
@AllArgsConstructor
public class CreateDealRequest {
    private String title;
    private Double value;
    private DealStage stage;
    private UUID leadId;
    private UUID assignedToUserId;
    private LocalDate expectedCloseDate;
    private UUID accountId;
}
