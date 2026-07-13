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
public class ExternalLeadResponse {
    private UUID leadId;
    private String name;
    private String email;
    private String message;
}
