package com.CRM.Repo;

import com.CRM.Entity.Task;
import com.CRM.Entity.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TaskRepo extends JpaRepository<Task , UUID> {

    Long countByAssignedToIdAndStatusNot(UUID userId, TaskStatus status);

    // 2. Fetch the immediate next 5 things I need to get done
    List<Task> findTop5ByAssignedToIdAndStatusNotOrderByDeadlineAsc(UUID userId, TaskStatus status);
}
