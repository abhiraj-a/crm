package com.CRM.DTO;

import com.CRM.Entity.TicketPriority;
import lombok.Data;

import java.util.UUID;

@Data
public class CreateTicketRequest {
    private String subject;
    private String description;
    private TicketPriority priority;
    private UUID leadId;
    private UUID assignedToUserId;
}
