package com.CRM.DTO;

import lombok.Data;

import java.util.UUID;

@Data
public class CreateNoteRequest {
    private String content;
    private UUID leadId;
    private UUID dealId;
}
