package com.CRM.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateNoteRequest {
    private String content;
    private UUID leadId;
    private UUID dealId;
}
