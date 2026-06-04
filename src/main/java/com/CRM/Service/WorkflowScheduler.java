package com.CRM.Service;

import com.CRM.Entity.ScheduledWorkflowAction;
import com.CRM.Entity.WorkflowExecution;
import com.CRM.Entity.WorkflowExecutionStatus;
import com.CRM.Repo.ScheduledWorkflowActionRepo;
import com.CRM.Repo.WorkflowExecutionRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class WorkflowScheduler {

    private final ScheduledWorkflowActionRepo scheduledWorkflowActionRepo;
    private final WorkflowExecutionRepo workflowExecutionRepo;
    private final WorkflowExecutionEngine executionEngine;

    /**
     * Polls every 60 seconds for scheduled workflow actions that are due.
     * When found, marks them as executed and resumes the workflow from the paused node.
     */
    @Scheduled(fixedRate = 60000)
    @Transactional
    public void pollScheduledActions() {
        List<ScheduledWorkflowAction> dueActions =
                scheduledWorkflowActionRepo.findByExecutedFalseAndScheduledTimeBefore(LocalDateTime.now());

        if (!dueActions.isEmpty()) {
            log.info("WorkflowScheduler found {} due actions to execute", dueActions.size());
        }

        for (ScheduledWorkflowAction action : dueActions) {
            try {
                WorkflowExecution execution = action.getWorkflowExecution();

                // Only resume if the execution is still paused for delay
                if (execution.getStatus() != WorkflowExecutionStatus.PAUSED_FOR_DELAY) {
                    log.warn("Skipping scheduled action {} — execution {} is in state {}",
                            action.getId(), execution.getId(), execution.getStatus());
                    action.setExecuted(true);
                    scheduledWorkflowActionRepo.save(action);
                    continue;
                }

                log.info("Resuming workflow execution {} from node {} (entity: {} {})",
                        execution.getId(),
                        action.getResumeFromNode().getId(),
                        action.getEntityType(),
                        action.getEntityId());

                // Mark the scheduled action as executed
                action.setExecuted(true);
                scheduledWorkflowActionRepo.save(action);

                // Resume the workflow from where it was paused
                executionEngine.resumeFromNode(
                        action.getResumeFromNode(),
                        action.getEntityType(),
                        action.getEntityId(),
                        execution
                );

            } catch (Exception e) {
                log.error("Failed to execute scheduled action {}: {}", action.getId(), e.getMessage());
            }
        }
    }
}
