package com.adro.tms_backend.controller;

import com.adro.tms_backend.dto.CategoryDto;
import com.adro.tms_backend.dto.CategoryRequest;
import com.adro.tms_backend.service.CategoryService;
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
@RequestMapping("/api/users/{userId}/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping
    public ResponseEntity<CategoryDto> create(
            @PathVariable UUID userId, @Valid @RequestBody CategoryRequest request) {
        CategoryDto created = categoryService.create(userId, request);
        return ResponseEntity.created(URI.create("/api/users/%s/categories/%s".formatted(userId, created.id())))
                .body(created);
    }

    @GetMapping
    public ResponseEntity<List<CategoryDto>> list(@PathVariable UUID userId) {
        return ResponseEntity.ok(categoryService.listByUser(userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryDto> findById(@PathVariable UUID userId, @PathVariable UUID id) {
        return ResponseEntity.ok(categoryService.findById(id, userId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoryDto> update(
            @PathVariable UUID userId, @PathVariable UUID id, @Valid @RequestBody CategoryRequest request) {
        return ResponseEntity.ok(categoryService.update(id, userId, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID userId, @PathVariable UUID id) {
        categoryService.delete(id, userId);
        return ResponseEntity.noContent().build();
    }
}
