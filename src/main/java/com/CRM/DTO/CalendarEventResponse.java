package com.CRM.DTO;

import com.CRM.Entity.CalendarEventType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class CalendarEventResponse {
    private UUID id;
    private String title;
    private String description;
    private CalendarEventType type;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime createdAt;
    
    private UUID assignedToUserId;
    private String assignedToUserName;
    
    private UUID relatedLeadId;
    private String relatedLeadName;
    
    private UUID relatedDealId;
    private String relatedDealTitle;
}
