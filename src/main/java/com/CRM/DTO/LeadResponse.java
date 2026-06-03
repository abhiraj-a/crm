package com.CRM.DTO;

import com.CRM.Entity.LeadStatus;
import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class LeadResponse {
    private UUID id;
    private String name;
    private String email;
    private String phone;
    private String company;
    private String source;
    private LeadStatus status;
    private Integer score;
    private LocalDateTime createdAt;
    private UUID assignedToUserId;
    private String assignedToUserName;
}
