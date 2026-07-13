package com.CRM.DTO;

import com.CRM.Entity.CalendarEventType;
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
public class CreateCalendarEventRequest {
    private String title;
    private String description;
    private CalendarEventType type;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    
    private UUID assignedToUserId;
    private UUID relatedLeadId;
    private UUID relatedDealId;
    private UUID relatedContactId;
}
