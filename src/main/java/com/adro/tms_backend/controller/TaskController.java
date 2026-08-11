package com.adro.tms_backend.controller;

import com.adro.tms_backend.dto.TaskCreateRequest;
import com.adro.tms_backend.dto.TaskDto;
import com.adro.tms_backend.dto.TaskUpdateRequest;
import com.adro.tms_backend.entity.enums.TaskPriority;
import com.adro.tms_backend.entity.enums.TaskStatus;
import com.adro.tms_backend.service.TaskService;
import jakarta.validation.Valid;
import java.net.URI;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users/{userId}/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @PostMapping
    public ResponseEntity<TaskDto> create(@PathVariable UUID userId, @Valid @RequestBody TaskCreateRequest request) {
        TaskDto created = taskService.create(userId, request);
        return ResponseEntity.created(URI.create("/api/users/%s/tasks/%s".formatted(userId, created.id())))
                .body(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaskDto> findById(@PathVariable UUID userId, @PathVariable UUID id) {
        return ResponseEntity.ok(taskService.findById(id, userId));
    }

    @GetMapping
    public ResponseEntity<Page<TaskDto>> list(
            @PathVariable UUID userId,
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(required = false) TaskPriority priority,
            @RequestParam(required = false) Boolean archived,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant dueFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant dueTo,
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(
                taskService.search(userId, status, priority, archived, dueFrom, dueTo, search, pageable));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TaskDto> update(
            @PathVariable UUID userId, @PathVariable UUID id, @Valid @RequestBody TaskUpdateRequest request) {
        return ResponseEntity.ok(taskService.update(id, userId, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID userId, @PathVariable UUID id) {
        taskService.softDelete(id, userId);
        return ResponseEntity.noContent().build();
    }
}
