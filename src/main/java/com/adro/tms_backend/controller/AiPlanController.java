package com.adro.tms_backend.controller;

import com.adro.tms_backend.dto.AiPlanDto;
import com.adro.tms_backend.service.AiPlanService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users/{userId}/ai-plans")
@RequiredArgsConstructor
public class AiPlanController {

    private final AiPlanService aiPlanService;

    @PostMapping("/generate")
    public ResponseEntity<AiPlanDto> generate(@PathVariable UUID userId) {
        return ResponseEntity.ok(aiPlanService.generate(userId));
    }

    @GetMapping("/latest")
    public ResponseEntity<AiPlanDto> getLatest(@PathVariable UUID userId) {
        AiPlanDto latest = aiPlanService.getLatest(userId);
        if (latest == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(latest);
    }
}
