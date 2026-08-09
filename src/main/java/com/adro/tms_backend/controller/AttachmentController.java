package com.adro.tms_backend.controller;

import com.adro.tms_backend.dto.AttachmentCreateRequest;
import com.adro.tms_backend.dto.AttachmentDto;
import com.adro.tms_backend.service.AttachmentService;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users/{userId}/tasks/{taskId}/attachments")
@RequiredArgsConstructor
public class AttachmentController {

    private final AttachmentService attachmentService;

    @PostMapping
    public ResponseEntity<AttachmentDto> create(
            @PathVariable UUID userId,
            @PathVariable UUID taskId,
            @Valid @RequestBody AttachmentCreateRequest request) {
        AttachmentDto created = attachmentService.create(taskId, userId, request);
        return ResponseEntity.created(URI.create(
                        "/api/users/%s/tasks/%s/attachments/%s".formatted(userId, taskId, created.id())))
                .body(created);
    }

    @GetMapping
    public ResponseEntity<List<AttachmentDto>> list(@PathVariable UUID userId, @PathVariable UUID taskId) {
        return ResponseEntity.ok(attachmentService.listByTask(taskId, userId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID userId, @PathVariable UUID taskId, @PathVariable UUID id) {
        attachmentService.delete(id, taskId, userId);
        return ResponseEntity.noContent().build();
    }
}
