package com.CRM.DTO;

import com.CRM.Entity.TicketPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateTicketRequest {
    @NotBlank(message = "Subject is required")
    @Size(max = 200, message = "Subject must be at most 200 characters")
    private String subject;

    @NotBlank(message = "Description is required")
    @Size(max = 2000, message = "Description must be at most 2000 characters")
    private String description;

    private TicketPriority priority;

    @NotNull(message = "Ticket source is required")
    private com.CRM.Entity.TicketSource source;

    @NotNull(message = "Lead ID is required")
    private UUID leadId;
    private UUID assignedToUserId;
}
