package com.CRM.Repo;

import com.CRM.Entity.Lead;
import com.CRM.Entity.LeadStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface LeadRepo extends JpaRepository<Lead, UUID> {
    Long countByOrganizationIdAndStatus(UUID organizationId, LeadStatus status);
    List<Lead> findByOrganizationId(UUID organizationId);
    List<Lead> findTop5ByOrganizationIdOrderByScoreDesc(UUID organizationId);

    List<Lead> findByAccountId(UUID accountId);
}
