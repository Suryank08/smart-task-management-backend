package com.adro.tms_backend.service;

import com.adro.tms_backend.dto.AiParserResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
public class AiParserService {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${app.gemini.api-key:}")
    private String apiKey;

    @Value("${app.gemini.url:https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent}")
    private String geminiUrl;

    public AiParserResponse parseTaskText(String text) {
        if (text == null || text.isBlank()) {
            return new AiParserResponse("", "", "MEDIUM", null, null, null);
        }

        if (apiKey != null && !apiKey.isBlank()) {
            try {
                return callGemini(text, apiKey);
            } catch (Exception e) {
                log.error("Failed to parse task using Gemini API, falling back to local parser", e);
            }
        }

        return parseLocally(text);
    }

    private AiParserResponse callGemini(String text, String apiKey) throws Exception {
        String url = geminiUrl + "?key=" + apiKey;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String prompt = """
            Analyze the following unstructured text, extract or infer the task details, and return a JSON object ONLY with the following schema:
            {
              "title": "string (a concise, clear summary of the task)",
              "description": "string (a professionally enhanced, detailed description summarizing the task's context, objectives, and expected actions based on the input)",
              "priority": "LOW|MEDIUM|HIGH|URGENT (if not explicitly specified, intelligently infer a reasonable priority based on urgency, impact, or keywords in the text)",
              "dueDate": "ISO8601 string or null (if relative like 'tomorrow' or 'today', resolve it using the current reference time: %s)",
              "estimatedMinutes": integer (if not explicitly specified, intelligently predict/estimate a realistic duration in minutes required to complete this task based on its complexity and nature)",
              "startDate": "ISO8601 string or null (if the text indicates a start date, return it, otherwise null)",
              "subtasks": ["string", "string", ...]   // optional list of subtask titles
            }
            Text: "%s"
            """.formatted(Instant.now().toString(), text.replace("\"", "\\\""));


        Map<String, Object> parts = new HashMap<>();
        parts.put("text", prompt);

        Map<String, Object> content = new HashMap<>();
        content.put("parts", List.of(parts));

        Map<String, Object> generationConfig = new HashMap<>();
        generationConfig.put("responseMimeType", "application/json");

        Map<String, Object> body = new HashMap<>();
        body.put("contents", List.of(content));
        body.put("generationConfig", generationConfig);

        String requestBody = objectMapper.writeValueAsString(body);
        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

        String responseStr = restTemplate.postForObject(url, entity, String.class);
        JsonNode responseNode = objectMapper.readTree(responseStr);
        
        JsonNode candidates = responseNode.path("candidates");
        if (candidates.isArray() && candidates.size() > 0) {
            JsonNode textNode = candidates.get(0).path("content").path("parts").get(0).path("text");
            String jsonOutput = textNode.asText();
            JsonNode parsedNode = objectMapper.readTree(jsonOutput);

            String title = parsedNode.path("title").asText("");
            String description = parsedNode.path("description").asText("");
            String priority = parsedNode.path("priority").asText("MEDIUM").toUpperCase();
            
            // Validate priority value
            if (!List.of("LOW", "MEDIUM", "HIGH", "URGENT").contains(priority)) {
                priority = "MEDIUM";
            }

            Instant dueDate = null;
            String dueDateStr = parsedNode.path("dueDate").asText("");
            if (!dueDateStr.isBlank() && !dueDateStr.equalsIgnoreCase("null")) {
                try {
                    dueDate = Instant.parse(dueDateStr);
                } catch (Exception e) {
                    log.warn("Failed to parse Gemini due date: {}", dueDateStr);
                }
            }

            Integer estimatedMinutes = null;
            if (parsedNode.has("estimatedMinutes") && !parsedNode.get("estimatedMinutes").isNull()) {
                estimatedMinutes = parsedNode.get("estimatedMinutes").asInt();
            }

            // Extract subtasks if present
            List<String> subtasks = new ArrayList<>();
            JsonNode subtasksNode = parsedNode.path("subtasks");
            if (subtasksNode.isArray()) {
                for (JsonNode subNode : subtasksNode) {
                    String sub = subNode.asText();
                    if (sub != null && !sub.isBlank()) {
                        subtasks.add(sub.trim());
                    }
                }
            }

            return new AiParserResponse(title, description, priority, dueDate, estimatedMinutes, subtasks);
        }

        throw new RuntimeException("Empty response from Gemini API");
    }

    private AiParserResponse parseLocally(String text) {
        String cleanText = text.trim();
        String lower = cleanText.toLowerCase();

        // 1. Title and Description
        String title;
        String description = cleanText;
        if (cleanText.length() <= 60) {
            title = cleanText;
        } else {
            int sentenceBound = cleanText.indexOf('.');
            if (sentenceBound > 10 && sentenceBound <= 80) {
                title = cleanText.substring(0, sentenceBound).trim();
            } else {
                title = cleanText.substring(0, 57).trim() + "...";
            }
        }

        // 2. Priority
        String priority = "MEDIUM";
        if (lower.contains("urgent") || lower.contains("critical") || lower.contains("asap")) {
            priority = "URGENT";
        } else if (lower.contains("high") || lower.contains("important") || lower.contains("must do")) {
            priority = "HIGH";
        } else if (lower.contains("low") || lower.contains("minor") || lower.contains("chill")) {
            priority = "LOW";
        }

        // 3. Estimated Minutes
        Integer estimatedMinutes = null;
        Pattern durationPattern = Pattern.compile("(\\d+)\\s*(hours|hour|hrs|hr|h|mins|min|minutes|minute)");
        Matcher durationMatcher = durationPattern.matcher(lower);
        if (durationMatcher.find()) {
            try {
                int value = Integer.parseInt(durationMatcher.group(1));
                String unit = durationMatcher.group(2);
                if (unit.startsWith("h")) {
                    estimatedMinutes = value * 60;
                } else {
                    estimatedMinutes = value;
                }
            } catch (Exception e) {
                log.warn("Failed to parse local duration", e);
            }
        }

        // 4. Due Date
        Instant dueDate = null;
        LocalDate localDue = null;
        LocalDate today = LocalDate.now();

        if (lower.contains("today")) {
            localDue = today;
        } else if (lower.contains("tomorrow")) {
            localDue = today.plusDays(1);
        } else if (lower.contains("day after tomorrow")) {
            localDue = today.plusDays(2);
        } else {
            // Check for pattern "in X days"
            Pattern daysPattern = Pattern.compile("in\\s+(\\d+)\\s+days");
            Matcher daysMatcher = daysPattern.matcher(lower);
            if (daysMatcher.find()) {
                try {
                    int days = Integer.parseInt(daysMatcher.group(1));
                    localDue = today.plusDays(days);
                } catch (Exception e) {
                    log.warn("Failed to parse relative days", e);
                }
            }
        }

        if (localDue != null) {
            // Default time to 11:59 PM (23:59:00) local time converted to Instant
            dueDate = localDue.atTime(23, 59, 0)
                    .atZone(ZoneId.systemDefault())
                    .toInstant();
        }

        // 5. Subtasks extraction (simple heuristic)
        List<String> subtasks = new ArrayList<>();
        int subIdx = lower.indexOf("subtasks:");
        if (subIdx != -1) {
            String subPart = cleanText.substring(subIdx + "subtasks:".length()).trim();
            // Split by commas or new lines
            String[] parts = subPart.split("[,:;]\s*");
            for (String part : parts) {
                String trimmed = part.trim();
                if (!trimmed.isEmpty()) {
                    subtasks.add(trimmed);
                }
            }
        }

        return new AiParserResponse(title, description, priority, dueDate, estimatedMinutes, subtasks);
    }
}
