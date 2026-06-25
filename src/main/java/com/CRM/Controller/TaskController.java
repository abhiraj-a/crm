package com.CRM.Controller;

import com.CRM.DTO.CreateTaskRequest;
import com.CRM.DTO.TaskResponse;
import com.CRM.DTO.UpdateTaskRequest;
import com.CRM.Entity.TaskStatus;
import com.CRM.Service.TaskService;
import com.CRM.Util.Principal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/task")
@RequiredArgsConstructor
public class TaskController {
    private final TaskService taskService;

    @PostMapping
    public ResponseEntity<?> createTask(@RequestBody CreateTaskRequest request, @AuthenticationPrincipal Principal principal) {
        try {
            return ResponseEntity.ok(taskService.createTask(request, principal.getAuthifyerId()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<List<TaskResponse>> getAllTasks(@AuthenticationPrincipal Principal principal) {
        // You could add logic here: if user is Admin return getAllOrganizationTasks, else return getMyTasks
        return ResponseEntity.ok(taskService.getAllOrganizationTasks(principal.getAuthifyerId()));
    }

    @GetMapping("/mine")
    public ResponseEntity<List<TaskResponse>> getMyTasks(@AuthenticationPrincipal Principal principal) {
        return ResponseEntity.ok(taskService.getMyTasks(principal.getAuthifyerId()));
    }

    @GetMapping("/{taskId}")
    public ResponseEntity<?> getTaskById(@PathVariable UUID taskId, @AuthenticationPrincipal Principal principal) {
        try {
            return ResponseEntity.ok(taskService.getTaskById(taskId, principal.getAuthifyerId()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{taskId}")
    public ResponseEntity<?> updateTask(@PathVariable UUID taskId, @RequestBody UpdateTaskRequest request, @AuthenticationPrincipal Principal principal) {
        try {
            return ResponseEntity.ok(taskService.updateTask(taskId, request, principal.getAuthifyerId()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{taskId}/status")
    public ResponseEntity<?> updateTaskStatus(@PathVariable UUID taskId, @RequestParam TaskStatus status, @AuthenticationPrincipal Principal principal) {
        try {
            return ResponseEntity.ok(taskService.updateTaskStatus(taskId, status, principal.getAuthifyerId()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{taskId}")
    public ResponseEntity<?> deleteTask(@PathVariable UUID taskId, @AuthenticationPrincipal Principal principal) {
        try {
            taskService.deleteTask(taskId, principal.getAuthifyerId());
            return ResponseEntity.ok(Map.of("message", "Task deleted successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
