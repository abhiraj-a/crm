package com.CRM.Controller;

import com.CRM.DTO.CalendarEventResponse;
import com.CRM.DTO.CreateCalendarEventRequest;
import com.CRM.DTO.UpdateCalendarEventRequest;
import com.CRM.Service.CalendarEventService;
import com.CRM.Util.Principal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/calendar-events")
@RequiredArgsConstructor
public class CalendarEventController {

    private final CalendarEventService calendarEventService;

    @GetMapping
    public ResponseEntity<List<CalendarEventResponse>> getEvents(@AuthenticationPrincipal Principal principal) {
        return ResponseEntity.ok(calendarEventService.getEventsForOrg(principal.getAuthifyerId()));
    }

    @PostMapping
    public ResponseEntity<?> createEvent(@RequestBody CreateCalendarEventRequest request, @AuthenticationPrincipal Principal principal) {
        try {
            return ResponseEntity.ok(calendarEventService.createEvent(request, principal.getAuthifyerId()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateEvent(@PathVariable UUID id, @RequestBody UpdateCalendarEventRequest request, @AuthenticationPrincipal Principal principal) {
        try {
            return ResponseEntity.ok(calendarEventService.updateEvent(id, request, principal.getAuthifyerId()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteEvent(@PathVariable UUID id, @AuthenticationPrincipal Principal principal) {
        try {
            calendarEventService.deleteEvent(id, principal.getAuthifyerId());
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
