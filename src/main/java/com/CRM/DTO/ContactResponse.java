package com.CRM.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContactResponse {
    private UUID id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String jobTitle;
    private UUID accountId;
    private String accountName;
    private UUID assignedToUserId;
    private String assignedToUserName;
    private LocalDateTime createdAt;
}
