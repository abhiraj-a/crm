package com.CRM.DTO;

import com.CRM.Entity.TicketPriority;
import com.CRM.Entity.TicketStatus;
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
public class TicketResponse {
    private UUID id;
    private String subject;
    private String description;
    private TicketStatus status;
    private TicketPriority priority;
    private com.CRM.Entity.TicketSource source;
    
    private UUID assignedToUserId;
    private String assignedToUserName;
    
    private UUID leadId;
    private String leadName;
    
    private LocalDateTime createdAt;
    private LocalDateTime closedAt;
}
