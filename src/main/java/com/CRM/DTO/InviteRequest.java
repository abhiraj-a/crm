package com.CRM.DTO;

import com.CRM.Entity.Role;
import lombok.Data;

@Data
public class InviteRequest {
    private String email;
    private Role role;
}
