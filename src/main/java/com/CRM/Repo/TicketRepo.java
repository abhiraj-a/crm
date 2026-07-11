package com.CRM.Repo;

import com.CRM.Entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface TicketRepo extends JpaRepository<Ticket, UUID> {
    List<Ticket> findByLeadIdOrderByCreatedAtDesc(UUID leadId);
    List<Ticket> findByOrganizationId(UUID organizationId);
}