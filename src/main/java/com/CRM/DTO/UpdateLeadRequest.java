package com.CRM.DTO;

import lombok.Getter;

@Getter
public class UpdateLeadRequest {
    private String name;
    private String email;
    private String phone;
    private String company;
    private Integer score;
}
