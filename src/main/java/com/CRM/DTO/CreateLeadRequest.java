package com.CRM.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateLeadRequest {
    private String name;
    private String email;
    private String phone;
    private String company;
    private String source;
    private Integer score;
    private UUID assignedToUserId;
    private UUID accountId;
}
