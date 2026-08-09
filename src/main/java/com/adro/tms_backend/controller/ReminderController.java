package com.adro.tms_backend.controller;

import com.adro.tms_backend.dto.ReminderDto;
import com.adro.tms_backend.dto.ReminderRequest;
import com.adro.tms_backend.service.ReminderService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users/{userId}/tasks/{taskId}/reminders")
@RequiredArgsConstructor
public class ReminderController {

    private final ReminderService reminderService;

    @PostMapping
    public ResponseEntity<ReminderDto> create(
            @PathVariable UUID userId, @PathVariable UUID taskId, @Valid @RequestBody ReminderRequest request) {
        ReminderDto created = reminderService.create(taskId, userId, request);
        return ResponseEntity.created(URI.create(
                        "/api/users/%s/tasks/%s/reminders/%s".formatted(userId, taskId, created.id())))
                .body(created);
    }

    @GetMapping
    public ResponseEntity<List<ReminderDto>> list(@PathVariable UUID userId, @PathVariable UUID taskId) {
        return ResponseEntity.ok(reminderService.listByTask(taskId, userId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ReminderDto> update(
            @PathVariable UUID userId,
            @PathVariable UUID taskId,
            @PathVariable UUID id,
            @Valid @RequestBody ReminderRequest request) {
        return ResponseEntity.ok(reminderService.update(id, taskId, userId, request));
    }

    @PatchMapping("/{id}/sent")
    public ResponseEntity<Void> markSent(
            @PathVariable UUID userId, @PathVariable UUID taskId, @PathVariable UUID id) {
        reminderService.markSent(id, taskId, userId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID userId, @PathVariable UUID taskId, @PathVariable UUID id) {
        reminderService.delete(id, taskId, userId);
        return ResponseEntity.noContent().build();
    }
}
