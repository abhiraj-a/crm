package com.CRM.Repo;

import com.CRM.Entity.ScheduledWorkflowAction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface ScheduledWorkflowActionRepo extends JpaRepository<ScheduledWorkflowAction, UUID> {
    List<ScheduledWorkflowAction> findByExecutedFalseAndScheduledTimeBefore(LocalDateTime now);
    List<ScheduledWorkflowAction> findByWorkflowExecutionIdIn(List<UUID> executionIds);
}
