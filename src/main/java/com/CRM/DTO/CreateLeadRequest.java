package com.CRM.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CreateLeadRequest {
    private String name;
    private String email;
    private String phone;
    private String company;
    private String source;
    private Integer score;
}
