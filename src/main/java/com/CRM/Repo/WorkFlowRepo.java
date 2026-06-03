package com.CRM.Repo;

import com.CRM.Entity.TriggerType;
import com.CRM.Entity.WorkFlow;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface WorkFlowRepo extends JpaRepository<WorkFlow, UUID> {
    // Find active workflows for a specific event in an organization
    List<WorkFlow> findByOrganizationIdAndTriggerTypeAndActiveTrue(UUID organizationId, TriggerType triggerType);
}