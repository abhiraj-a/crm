package com.CRM.DTO;

import com.CRM.Entity.InteractionType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateInteractionRequest {
    private InteractionType type;
    private String subject;
    private String details;
    private UUID leadId;
}
