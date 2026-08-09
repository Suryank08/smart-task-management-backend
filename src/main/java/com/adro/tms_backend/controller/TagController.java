package com.adro.tms_backend.controller;

import com.adro.tms_backend.dto.TagDto;
import com.adro.tms_backend.dto.TagRequest;
import com.adro.tms_backend.service.TagService;
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
@RequestMapping("/api/users/{userId}/tags")
@RequiredArgsConstructor
public class TagController {

    private final TagService tagService;

    @PostMapping
    public ResponseEntity<TagDto> create(@PathVariable UUID userId, @Valid @RequestBody TagRequest request) {
        TagDto created = tagService.create(userId, request);
        return ResponseEntity.created(URI.create("/api/users/%s/tags/%s".formatted(userId, created.id())))
                .body(created);
    }

    @GetMapping
    public ResponseEntity<List<TagDto>> list(@PathVariable UUID userId) {
        return ResponseEntity.ok(tagService.listByUser(userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TagDto> findById(@PathVariable UUID userId, @PathVariable UUID id) {
        return ResponseEntity.ok(tagService.findById(id, userId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TagDto> update(
            @PathVariable UUID userId, @PathVariable UUID id, @Valid @RequestBody TagRequest request) {
        return ResponseEntity.ok(tagService.update(id, userId, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID userId, @PathVariable UUID id) {
        tagService.delete(id, userId);
        return ResponseEntity.noContent().build();
    }
}
