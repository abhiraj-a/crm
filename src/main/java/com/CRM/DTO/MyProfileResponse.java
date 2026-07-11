package com.CRM.DTO;

import com.CRM.Entity.Role;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class MyProfileResponse {
    private UUID id;
    private String firstName;
    private String lastName;
    private String email;
    private Role role;
    private String jobTitle;
    private String orgName;
    private UUID orgId;
}
