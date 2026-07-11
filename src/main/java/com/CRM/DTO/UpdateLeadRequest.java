package com.CRM.DTO;

import com.CRM.Entity.LeadStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateLeadRequest {
    private String name;
    private String email;
    private String phone;
    private String company;
    private String source;
    private LeadStatus status;
    private Integer score;
    private UUID assignedToUserId;
}
