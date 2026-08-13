package com.adro.tms_backend.controller;

import com.adro.tms_backend.dto.AiParserRequest;
import com.adro.tms_backend.dto.AiParserResponse;
import com.adro.tms_backend.service.AiParserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiParserController {

    private final AiParserService aiParserService;

    @PostMapping("/parse-task")
    public ResponseEntity<AiParserResponse> parseTask(@Valid @RequestBody AiParserRequest request) {
        AiParserResponse parsed = aiParserService.parseTaskText(request.text());
        return ResponseEntity.ok(parsed);
    }
}
