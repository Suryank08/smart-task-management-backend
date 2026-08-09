package com.adro.tms_backend.service;

import com.adro.tms_backend.dto.AiSuggestionCreateRequest;
import com.adro.tms_backend.dto.AiSuggestionDto;
import com.adro.tms_backend.dto.AiSuggestionResolveRequest;
import com.adro.tms_backend.entity.AiSuggestion;
import com.adro.tms_backend.entity.Task;
import com.adro.tms_backend.entity.enums.UserStatus;
import com.adro.tms_backend.exception.ResourceNotFoundException;
import com.adro.tms_backend.mapper.AiSuggestionMapper;
import com.adro.tms_backend.repository.AiSuggestionRepository;
import com.adro.tms_backend.repository.TaskRepository;
import com.adro.tms_backend.repository.UserRepository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AiSuggestionService {

    private final AiSuggestionRepository aiSuggestionRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final AiSuggestionMapper aiSuggestionMapper;

    @Transactional
    public AiSuggestionDto create(UUID taskId, UUID userId, AiSuggestionCreateRequest request) {
        Task task = getOwnedTask(taskId, userId);

        AiSuggestion suggestion = AiSuggestion.builder()
                .task(task)
                .type(request.type())
                .payload(request.payload())
                .build();

        return aiSuggestionMapper.toDto(aiSuggestionRepository.save(suggestion));
    }

    @Transactional(readOnly = true)
    public List<AiSuggestionDto> listByTask(UUID taskId, UUID userId) {
        getOwnedTask(taskId, userId);
        return aiSuggestionMapper.toDtoList(aiSuggestionRepository.findByTaskIdOrderByCreatedAtDesc(taskId));
    }

    @Transactional
    public AiSuggestionDto resolve(UUID id, UUID taskId, UUID userId, AiSuggestionResolveRequest request) {
        getOwnedTask(taskId, userId);
        AiSuggestion suggestion = aiSuggestionRepository
                .findByIdAndTaskId(id, taskId)
                .orElseThrow(() -> ResourceNotFoundException.of("AiSuggestion", id));

        suggestion.setStatus(request.status());
        suggestion.setResolvedAt(Instant.now());

        return aiSuggestionMapper.toDto(aiSuggestionRepository.save(suggestion));
    }

    private Task getOwnedTask(UUID taskId, UUID userId) {
        requireActiveUser(userId);
        Task task = taskRepository
                .findByIdAndDeletedAtIsNull(taskId)
                .orElseThrow(() -> ResourceNotFoundException.of("Task", taskId));
        if (!task.getUser().getId().equals(userId)) {
            throw ResourceNotFoundException.of("Task", taskId);
        }
        return task;
    }

    private void requireActiveUser(UUID userId) {
        if (!userRepository.existsByIdAndStatus(userId, UserStatus.ACTIVE)) {
            throw ResourceNotFoundException.of("User", userId);
        }
    }
}
