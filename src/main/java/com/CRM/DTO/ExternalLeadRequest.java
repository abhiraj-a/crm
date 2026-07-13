package com.CRM.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExternalLeadRequest {
    private String name;
    private String email;
    private String phone;
    private String company;
    private String source; // E.g., "Website Contact Form"
    private String message;
}
