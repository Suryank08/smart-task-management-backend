package com.adro.tms_backend.controller;

import com.adro.tms_backend.dto.RecurringPatternDto;
import com.adro.tms_backend.dto.RecurringPatternRequest;
import com.adro.tms_backend.service.RecurringPatternService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users/{userId}/tasks/{taskId}/recurring-pattern")
@RequiredArgsConstructor
public class RecurringPatternController {

    private final RecurringPatternService recurringPatternService;

    @PutMapping
    public ResponseEntity<RecurringPatternDto> upsert(
            @PathVariable UUID userId,
            @PathVariable UUID taskId,
            @Valid @RequestBody RecurringPatternRequest request) {
        return ResponseEntity.ok(recurringPatternService.upsert(taskId, userId, request));
    }

    @GetMapping
    public ResponseEntity<RecurringPatternDto> getByTask(@PathVariable UUID userId, @PathVariable UUID taskId) {
        return ResponseEntity.ok(recurringPatternService.getByTask(taskId, userId));
    }

    @DeleteMapping
    public ResponseEntity<Void> delete(@PathVariable UUID userId, @PathVariable UUID taskId) {
        recurringPatternService.delete(taskId, userId);
        return ResponseEntity.noContent().build();
    }
}
