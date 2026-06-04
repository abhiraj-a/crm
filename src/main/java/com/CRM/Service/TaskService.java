package com.CRM.Service;

import com.CRM.DTO.CreateTaskRequest;
import com.CRM.DTO.TaskResponse;
import com.CRM.DTO.UpdateTaskRequest;
import com.CRM.Entity.*;
import com.CRM.Repo.DealRepo;
import com.CRM.Repo.LeadRepo;
import com.CRM.Repo.TaskRepo;
import com.CRM.Repo.UserRepo;
import com.CRM.Event.TaskCompletedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskService {
    private final TaskRepo taskRepo;
    private final UserRepo userRepo;
    private final LeadRepo leadRepo;
    private final DealRepo dealRepo;
    private final ApplicationEventPublisher eventPublisher;


    private User getCurrentUser(String authifyerId) {
        return userRepo.findByAuthifyerId(authifyerId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private Task getAuthorizedTask(UUID taskId, User currentUser) {
        Task task = taskRepo.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));
        if (!task.getOrganization().getId().equals(currentUser.getOrganization().getId())) {
            throw new RuntimeException("Unauthorized access: Task belongs to a different organization.");
        }
        return task;
    }

    // --- THE MAPPER ---
    private TaskResponse mapToResponse(Task task) {
        return TaskResponse.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .status(task.getStatus())
                .deadline(task.getDeadline())
                .createdAt(task.getCreatedAt())

                .assignedToUserId(task.getAssignedTo() != null ? task.getAssignedTo().getId() : null)
                .assignedToUserName(task.getAssignedTo() != null ?
                        task.getAssignedTo().getFirstName() + " " + task.getAssignedTo().getLastName() : "Unassigned")

                .relatedLeadId(task.getRelatedLead() != null ? task.getRelatedLead().getId() : null)
                .relatedLeadName(task.getRelatedLead() != null ? task.getRelatedLead().getName() : null)

                .relatedDealId(task.getRelatedDeal() != null ? task.getRelatedDeal().getId() : null)
                .relatedDealTitle(task.getRelatedDeal() != null ? task.getRelatedDeal().getTitle() : null)
                .build();
    }


    @Transactional
    public TaskResponse createTask(CreateTaskRequest request, String authifyerId) {
        User currentUser = getCurrentUser(authifyerId);

        User assignee = request.getAssignedToUserId() != null ?
                userRepo.findById(request.getAssignedToUserId()).orElse(currentUser) : currentUser;

        Lead relatedLead = request.getRelatedLeadId() != null ?
                leadRepo.findById(request.getRelatedLeadId()).orElse(null) : null;

        Deal relatedDeal = request.getRelatedDealId() != null ?
                dealRepo.findById(request.getRelatedDealId()).orElse(null) : null;

        Task task = Task.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .deadline(request.getDeadline())
                .status(TaskStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .organization(currentUser.getOrganization())
                .assignedTo(assignee)
                .relatedLead(relatedLead)
                .relatedDeal(relatedDeal)
                .build();

        return mapToResponse(taskRepo.save(task));
    }

    public List<TaskResponse> getAllOrganizationTasks(String authifyerId) {
        User currentUser = getCurrentUser(authifyerId);
        return taskRepo.findByOrganizationId(currentUser.getOrganization().getId())
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public List<TaskResponse> getMyTasks(String authifyerId) {
        User currentUser = getCurrentUser(authifyerId);
        return taskRepo.findByAssignedToId(currentUser.getId())
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public TaskResponse getTaskById(UUID taskId, String authifyerId) {
        User currentUser = getCurrentUser(authifyerId);
        return mapToResponse(getAuthorizedTask(taskId, currentUser));
    }

    @Transactional
    public TaskResponse updateTask(UUID taskId, UpdateTaskRequest request, String authifyerId) {
        User currentUser = getCurrentUser(authifyerId);
        Task task = getAuthorizedTask(taskId, currentUser);

        if (request.getTitle() != null) task.setTitle(request.getTitle());
        if (request.getDescription() != null) task.setDescription(request.getDescription());
        if (request.getDeadline() != null) task.setDeadline(request.getDeadline());

        if (request.getAssignedToUserId() != null) {
            userRepo.findById(request.getAssignedToUserId()).ifPresent(task::setAssignedTo);
        }
        if (request.getRelatedLeadId() != null) {
            leadRepo.findById(request.getRelatedLeadId()).ifPresent(task::setRelatedLead);
        }
        if (request.getRelatedDealId() != null) {
            dealRepo.findById(request.getRelatedDealId()).ifPresent(task::setRelatedDeal);
        }

        return mapToResponse(taskRepo.save(task));
    }

    @Transactional
    public TaskResponse updateTaskStatus(UUID taskId, TaskStatus newStatus, String authifyerId) {
        User currentUser = getCurrentUser(authifyerId);
        Task task = getAuthorizedTask(taskId, currentUser);

        task.setStatus(newStatus);
        Task savedTask = taskRepo.save(task);

        // Fire event for workflow automation when a task is completed
        if (newStatus == TaskStatus.COMPLETED) {
            eventPublisher.publishEvent(new TaskCompletedEvent(this, savedTask, authifyerId));
        }

        return mapToResponse(savedTask);
    }

    @Transactional
    public void deleteTask(UUID taskId, String authifyerId) {
        User currentUser = getCurrentUser(authifyerId);
        Task task = getAuthorizedTask(taskId, currentUser);
        taskRepo.delete(task);
    }
}
