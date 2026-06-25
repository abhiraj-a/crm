package com.CRM.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateAccountRequest {
    private String companyName;
    private String industry;
    private String website;
    private String employeeCount;
    private String annualRevenue;
    private String description;
    private UUID parentAccountId;
}
