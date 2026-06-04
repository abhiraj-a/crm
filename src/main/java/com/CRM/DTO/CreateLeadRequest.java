package com.CRM.DTO;

import lombok.Getter;

@Getter
public class CreateLeadRequest {
    private String name;
    private String email;
    private String phone;
    private String company;
    private String source;
    private Integer score;
}
