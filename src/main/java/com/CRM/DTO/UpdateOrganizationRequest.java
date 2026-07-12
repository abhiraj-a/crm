package com.CRM.DTO;

import lombok.Data;

@Data
public class UpdateOrganizationRequest {
    private String companyName;
    private String companyAddress;
    private Integer companySize;
}
