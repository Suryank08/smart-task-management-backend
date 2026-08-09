package com.adro.tms_backend.controller;

import com.adro.tms_backend.dto.SubtaskDto;
import com.adro.tms_backend.dto.SubtaskRequest;
import com.adro.tms_backend.service.SubtaskService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users/{userId}/tasks/{taskId}/subtasks")
@RequiredArgsConstructor
public class SubtaskController {

    private final SubtaskService subtaskService;

    @PostMapping
    public ResponseEntity<SubtaskDto> create(
            @PathVariable UUID userId, @PathVariable UUID taskId, @Valid @RequestBody SubtaskRequest request) {
        SubtaskDto created = subtaskService.create(taskId, userId, request);
        return ResponseEntity.created(URI.create(
                        "/api/users/%s/tasks/%s/subtasks/%s".formatted(userId, taskId, created.id())))
                .body(created);
    }

    @GetMapping
    public ResponseEntity<List<SubtaskDto>> list(@PathVariable UUID userId, @PathVariable UUID taskId) {
        return ResponseEntity.ok(subtaskService.listByTask(taskId, userId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SubtaskDto> update(
            @PathVariable UUID userId,
            @PathVariable UUID taskId,
            @PathVariable UUID id,
            @Valid @RequestBody SubtaskRequest request) {
        return ResponseEntity.ok(subtaskService.update(id, taskId, userId, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID userId, @PathVariable UUID taskId, @PathVariable UUID id) {
        subtaskService.delete(id, taskId, userId);
        return ResponseEntity.noContent().build();
    }
}
