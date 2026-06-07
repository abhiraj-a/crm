package com.CRM.DTO;

import lombok.Data;

import java.util.UUID;

@Data
public class CreateAccountRequest {
    private String companyName;
    private String industry;
    private String website;
    private String employeeCount;
    private String annualRevenue;
    private String description;
    private UUID parentAccountId;
}
