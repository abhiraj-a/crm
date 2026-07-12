package com.CRM.DTO;

import lombok.Data;

@Data
public class UpdateProfileRequest {
    private String firstName;
    private String lastName;
    private String jobTitle;
    private String phoneNumber;
}
