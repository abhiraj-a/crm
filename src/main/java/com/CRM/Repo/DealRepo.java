package com.CRM.Repo;

import com.CRM.Entity.Deal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface DealRepo  extends JpaRepository<Deal, UUID> {

    @Query("SELECT SUM(d.value) FROM Deal d WHERE d.organization.id = :orgId")
    Double sumTotalPipelineValue(@Param("orgId") UUID orgId);
}
