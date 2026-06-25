package com.CRM.DTO;

import com.CRM.Entity.TicketPriority;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateTicketRequest {
    private String subject;
    private String description;
    private TicketPriority priority;
    private UUID leadId;
    private UUID assignedToUserId;
}
