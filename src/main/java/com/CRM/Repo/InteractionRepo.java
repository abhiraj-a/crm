package com.CRM.Repo;

import com.CRM.Entity.Interaction;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface InteractionRepo extends JpaRepository<Interaction, UUID> {
    List<Interaction> findByLeadIdOrderByTimestampDesc(UUID leadId);
}