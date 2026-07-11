package com.CRM.DTO;

import com.CRM.Entity.Role;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class InviteResponse {
    private UUID id;
    private String email;
    private Role role;
    private String invitedByName;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
}
