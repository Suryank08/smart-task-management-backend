package com.adro.tms_backend.service;

import com.adro.tms_backend.dto.AiPlanDto;
import com.adro.tms_backend.entity.AiPlan;
import com.adro.tms_backend.entity.Task;
import com.adro.tms_backend.entity.User;
import com.adro.tms_backend.entity.enums.TaskPriority;
import com.adro.tms_backend.entity.enums.TaskStatus;
import com.adro.tms_backend.entity.enums.UserStatus;
import com.adro.tms_backend.exception.ResourceNotFoundException;
import com.adro.tms_backend.mapper.AiPlanMapper;
import com.adro.tms_backend.repository.AiPlanRepository;
import com.adro.tms_backend.repository.TaskRepository;
import com.adro.tms_backend.repository.UserRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiPlanService {

    private final AiPlanRepository aiPlanRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final AiPlanMapper aiPlanMapper;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${app.gemini.api-key:}")
    private String apiKey;

    @Value("${app.gemini.url:https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent}")
    private String geminiUrl;

    @Transactional(readOnly = true)
    public AiPlanDto getLatest(UUID userId) {
        requireActiveUser(userId);
        return aiPlanRepository.findFirstByUserIdOrderByCreatedAtDesc(userId)
                .map(aiPlanMapper::toDto)
                .orElse(null);
    }

    @Transactional
    public AiPlanDto generate(UUID userId) {
        User user = userRepository.findByIdAndStatus(userId, UserStatus.ACTIVE)
                .orElseThrow(() -> ResourceNotFoundException.of("User", userId));

        List<Task> activeTasks = taskRepository.findByUserIdAndStatusInAndDeletedAtIsNull(
                userId, List.of(TaskStatus.PENDING, TaskStatus.IN_PROGRESS));

        if (activeTasks.isEmpty()) {
            // Save and return empty plan
            Map<String, Object> emptyPlan = new HashMap<>();
            emptyPlan.put("explanation", "You have no active or incomplete tasks. Add some tasks first to make a plan!");
            emptyPlan.put("tasks", List.of());

            AiPlan planEntity = AiPlan.builder()
                    .user(user)
                    .planDetails(emptyPlan)
                    .build();
            return aiPlanMapper.toDto(aiPlanRepository.save(planEntity));
        }

        Map<String, Object> planDetails;
        if (apiKey != null && !apiKey.isBlank()) {
            try {
                planDetails = callGeminiForPlan(activeTasks);
            } catch (Exception e) {
                log.error("Failed to generate AI plan using Gemini API, falling back to local optimization", e);
                planDetails = generateLocalPlan(activeTasks);
            }
        } else {
            planDetails = generateLocalPlan(activeTasks);
        }

        AiPlan planEntity = AiPlan.builder()
                .user(user)
                .planDetails(planDetails)
                .build();

        return aiPlanMapper.toDto(aiPlanRepository.save(planEntity));
    }

    private Map<String, Object> callGeminiForPlan(List<Task> tasks) throws Exception {
        String url = geminiUrl + "?key=" + apiKey;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        StringBuilder tasksListStr = new StringBuilder();
        for (Task t : tasks) {
            tasksListStr.append("- Task ID: ").append(t.getId())
                    .append(", Title: \"").append(t.getTitle().replace("\"", "\\\"")).append("\"")
                    .append(", Priority: ").append(t.getPriority())
                    .append(", Due Date: ").append(t.getDueDate() != null ? t.getDueDate().toString() : "None")
                    .append(", Estimated Time: ").append(t.getEstimatedMinutes() != null ? t.getEstimatedMinutes() + " mins" : "None")
                    .append(", Description: \"").append(t.getDescription() != null ? t.getDescription().replace("\"", "\\\"") : "").append("\"\n");
        }

        String prompt = """
            You are a productivity expert. Create an optimized, ordered daily plan/schedule to make the user's day highly productive based on their active/incomplete tasks.
            Analyze the tasks and prioritize them considering:
            1. Deadlines / Due Dates (tasks due today or overdue first)
            2. Priority levels (URGENT > HIGH > MEDIUM > LOW)
            3. Estimated time (e.g. schedule quick wins, block time for deep work)
            
            Return a JSON object ONLY with the following schema:
            {
              "explanation": "string (a professional, encouraging summary of the plan, explaining the strategy and tips for today's tasks)",
              "tasks": [
                {
                  "taskId": "string (UUID)",
                  "suggestedOrder": integer (1-based),
                  "reason": "string (a brief, clear explanation of why this task was scheduled here)"
                }
              ]
            }
            
            Tasks to plan:
            %s
            """.formatted(tasksListStr.toString());

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
            return objectMapper.readValue(jsonOutput, new TypeReference<Map<String, Object>>() {});
        }

        throw new RuntimeException("Empty response from Gemini API");
    }

    private Map<String, Object> generateLocalPlan(List<Task> tasks) {
        // Sort: Urgent > High > Medium > Low
        // If priority same, sort by due date (closest first, nulls last)
        // If due date same, sort by estimated time (shorter first, nulls last)
        List<Task> sorted = new ArrayList<>(tasks);
        sorted.sort((t1, t2) -> {
            int p1 = getPriorityWeight(t1.getPriority());
            int p2 = getPriorityWeight(t2.getPriority());
            if (p1 != p2) {
                return Integer.compare(p2, p1); // Higher priority weight first
            }

            Instant d1 = t1.getDueDate();
            Instant d2 = t2.getDueDate();
            if (d1 != null && d2 != null) {
                int dueCompare = d1.compareTo(d2);
                if (dueCompare != 0) return dueCompare;
            } else if (d1 != null) {
                return -1;
            } else if (d2 != null) {
                return 1;
            }

            Integer e1 = t1.getEstimatedMinutes();
            Integer e2 = t2.getEstimatedMinutes();
            if (e1 != null && e2 != null) {
                int estCompare = e1.compareTo(e2);
                if (estCompare != 0) return estCompare;
            } else if (e1 != null) {
                return -1;
            } else if (e2 != null) {
                return 1;
            }

            return t1.getTitle().compareTo(t2.getTitle());
        });

        List<Map<String, Object>> plannedTasks = new ArrayList<>();
        for (int i = 0; i < sorted.size(); i++) {
            Task t = sorted.get(i);
            Map<String, Object> pt = new HashMap<>();
            pt.put("taskId", t.getId().toString());
            pt.put("suggestedOrder", i + 1);
            pt.put("reason", "Scheduled based on priority (" + t.getPriority() + ")" +
                    (t.getDueDate() != null ? " and deadline" : "") +
                    (t.getEstimatedMinutes() != null ? ", estimated at " + t.getEstimatedMinutes() + " mins" : "") + ".");
            plannedTasks.add(pt);
        }

        Map<String, Object> plan = new HashMap<>();
        plan.put("explanation", "Here is your locally-optimized daily plan. Tasks are sequenced prioritizing urgency, upcoming deadlines, and short estimated durations to build quick momentum.");
        plan.put("tasks", plannedTasks);
        return plan;
    }

    private int getPriorityWeight(TaskPriority priority) {
        if (priority == null) return 1;
        return switch (priority) {
            case URGENT -> 4;
            case HIGH -> 3;
            case MEDIUM -> 2;
            case LOW -> 1;
        };
    }

    private void requireActiveUser(UUID userId) {
        if (!userRepository.existsByIdAndStatus(userId, UserStatus.ACTIVE)) {
            throw ResourceNotFoundException.of("User", userId);
        }
    }
}
