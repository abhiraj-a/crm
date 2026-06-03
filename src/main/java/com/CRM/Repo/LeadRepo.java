package com.CRM.Repo;

import com.CRM.Entity.Lead;
import com.CRM.Entity.LeadStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface LeadRepo extends JpaRepository<Lead, UUID> {
    Long countByOrganizationIdAndStatus(String organizationId, LeadStatus status);

    List<Lead> findTop5ByOrganizationIdOrderByScoreDesc(String organizationId);
}
