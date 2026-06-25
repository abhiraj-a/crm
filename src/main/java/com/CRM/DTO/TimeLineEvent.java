package com.CRM.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimeLineEvent {
    private UUID eventId;

    // e.g., "NOTE", "CALL", "EMAIL", "MEETING", "TICKET_OPENED", "TASK_COMPLETED"
    private String eventType;

    private String title;
    private String description;
    private LocalDateTime timestamp;
    private String authorName;
}