package com.CRM.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConvertLeadRequest {
    private UUID accountId;
    private String newAccountName;
    private CreateAccountRequest newAccountDetails;
    private boolean createDeal;
    private String dealName;
    private Double dealValue;
}
