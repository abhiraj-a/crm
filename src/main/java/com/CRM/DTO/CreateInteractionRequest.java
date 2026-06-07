package com.CRM.DTO;

import com.CRM.Entity.InteractionType;
import lombok.Data;

import java.util.UUID;

@Data
public class CreateInteractionRequest {
    private InteractionType type;
    private String subject;
    private String details;
    private UUID leadId;
}
