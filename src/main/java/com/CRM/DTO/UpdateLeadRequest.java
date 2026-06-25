package com.CRM.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateLeadRequest {
    private String name;
    private String email;
    private String phone;
    private String company;
    private Integer score;
}
