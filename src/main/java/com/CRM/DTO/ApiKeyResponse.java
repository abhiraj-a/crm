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
public class ApiKeyResponse {
    private UUID id;
    private String name;
    private String keyPrefix;
    private LocalDateTime createdAt;
    private LocalDateTime lastUsedAt;
    private boolean active;
    
    // Only populated on creation
    private String rawKey;
}
