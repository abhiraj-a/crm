package com.CRM.Repo;

import com.CRM.Entity.Deal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface DealRepo extends JpaRepository<Deal, UUID> {

    @Query("SELECT SUM(d.value) FROM Deal d WHERE d.organization.id = :orgId")
    Double sumTotalPipelineValue(@Param("orgId") UUID orgId);

    List<Deal> findByOrganizationId(UUID organizationId);

    List<Deal> findByAccountId(UUID accountId);

    List<Deal> findByLeadId(UUID leadId);
}
