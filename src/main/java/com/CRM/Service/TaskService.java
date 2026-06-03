package com.CRM.Service;

import com.CRM.DTO.CreateTaskRequest;
import com.CRM.Entity.*;
import com.CRM.Repo.DealRepo;
import com.CRM.Repo.LeadRepo;
import com.CRM.Repo.TaskRepo;
import com.CRM.Repo.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepo taskRepo;
    private final UserRepo userRepo;
    private final LeadRepo leadRepo;
    private final DealRepo dealRepo;

    @Transactional
    public Task createTask(CreateTaskRequest request, String authifyerId) {
        // 1. Identify the logged-in user and their organization
        User currentUser = userRepo.findByAuthifyerId(authifyerId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Task task = Task.builder()
                .createdAt(LocalDateTime.now())
                .title(request.getTitle())
                .deadline(request.getDeadline())
                .description(request.getDescription())
                .status(TaskStatus.PENDING)
                .organization(currentUser.getOrganization())
                .build();


        // 2. Assign the task (defaults to the person creating it if no ID is provided)
        if (request.getAssignedToUserId() != null) {
            User assignee = userRepo.findById(request.getAssignedToUserId())
                    .orElseThrow(() -> new RuntimeException("Assignee not found"));
            task.setAssignedTo(assignee);
        } else {
            task.setAssignedTo(currentUser);
        }

        // 3. Link to a Lead (if provided)
        if (request.getRelatedLeadId() != null) {
            Lead lead = leadRepo.findById(request.getRelatedLeadId())
                    .orElseThrow(() -> new RuntimeException("Lead not found"));
            task.setRelatedLead(lead);
        }

        // 4. Link to a Deal (if provided)
        if (request.getRelatedDealId() != null) {
            Deal deal = dealRepo.findById(request.getRelatedDealId())
                    .orElseThrow(() -> new RuntimeException("Deal not found"));
            task.setRelatedDeal(deal);
        }

        return taskRepo.save(task);
    }

    @Transactional
    public void deleteTask(UUID taskId, String authifyerId) {
        User currentUser = userRepo.findByAuthifyerId(authifyerId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Task task = taskRepo.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        // Security Check: Ensure the task belongs to the user's organization
        if (!task.getOrganization().getId().equals(currentUser.getOrganization().getId())) {
            throw new RuntimeException("Unauthorized: Cannot delete tasks outside your organization.");
        }

        taskRepo.delete(task);
    }
}
