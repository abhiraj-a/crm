package com.CRM.Repo;

import com.CRM.Entity.Task;
import com.CRM.Entity.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TaskRepo extends JpaRepository<Task , UUID> {

    Long countByAssignedToIdAndStatusNot(UUID userId, TaskStatus status);
    List<Task> findTop5ByAssignedToIdAndStatusNotOrderByDeadlineAsc(UUID userId, TaskStatus status);

    // Fetch all tasks in the workspace
    List<Task> findByOrganizationId(UUID organizationId);

    // Fetch tasks specifically assigned to a single user
    List<Task> findByAssignedToId(UUID userId);

    List<Task> findByRelatedLeadId(UUID leadId);
    
    List<Task> findByRelatedDealId(UUID dealId);
}
