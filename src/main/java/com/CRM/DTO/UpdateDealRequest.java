package com.CRM.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateDealRequest {
    private String title;
    private Double value;
    private UUID leadId;
    private UUID assignedToUserId;
    private LocalDate expectedCloseDate;
    private UUID accountId;
}
