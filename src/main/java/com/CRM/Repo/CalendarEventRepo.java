package com.CRM.Repo;

import com.CRM.Entity.CalendarEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface CalendarEventRepo extends JpaRepository<CalendarEvent, UUID> {
    
    List<CalendarEvent> findByOrganizationId(UUID organizationId);
    
    List<CalendarEvent> findByOrganizationIdAndStartTimeBetweenOrderByStartTimeAsc(
            UUID organizationId, LocalDateTime start, LocalDateTime end);
            
    List<CalendarEvent> findByRelatedLeadId(UUID leadId);
}
