package com.adro.tms_backend.controller;

import com.adro.tms_backend.dto.AiSuggestionCreateRequest;
import com.adro.tms_backend.dto.AiSuggestionDto;
import com.adro.tms_backend.dto.AiSuggestionResolveRequest;
import com.adro.tms_backend.service.AiSuggestionService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users/{userId}/tasks/{taskId}/ai-suggestions")
@RequiredArgsConstructor
public class AiSuggestionController {

    private final AiSuggestionService aiSuggestionService;

    @PostMapping
    public ResponseEntity<AiSuggestionDto> create(
            @PathVariable UUID userId,
            @PathVariable UUID taskId,
            @Valid @RequestBody AiSuggestionCreateRequest request) {
        AiSuggestionDto created = aiSuggestionService.create(taskId, userId, request);
        return ResponseEntity.created(URI.create(
                        "/api/users/%s/tasks/%s/ai-suggestions/%s".formatted(userId, taskId, created.id())))
                .body(created);
    }

    @GetMapping
    public ResponseEntity<List<AiSuggestionDto>> list(@PathVariable UUID userId, @PathVariable UUID taskId) {
        return ResponseEntity.ok(aiSuggestionService.listByTask(taskId, userId));
    }

    @PatchMapping("/{id}/resolve")
    public ResponseEntity<AiSuggestionDto> resolve(
            @PathVariable UUID userId,
            @PathVariable UUID taskId,
            @PathVariable UUID id,
            @Valid @RequestBody AiSuggestionResolveRequest request) {
        return ResponseEntity.ok(aiSuggestionService.resolve(id, taskId, userId, request));
    }
}
