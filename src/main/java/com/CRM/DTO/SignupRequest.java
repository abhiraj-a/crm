package com.CRM.DTO;

import lombok.Data;

@Data
public class SignupRequest {
    private String password;
    private String email;
    private String authifyerId;
    private String firstName;
    private String lasName;
    private String companyName;
    private String companyAddress;
    private int companySize;
    private String phoneNumber;
    private String jobTitle;
}
