package com.adro.tms_backend.service;

import com.adro.tms_backend.dto.RecurringPatternDto;
import com.adro.tms_backend.dto.RecurringPatternRequest;
import com.adro.tms_backend.entity.RecurringPattern;
import com.adro.tms_backend.entity.Task;
import com.adro.tms_backend.entity.enums.UserStatus;
import com.adro.tms_backend.exception.ResourceNotFoundException;
import com.adro.tms_backend.mapper.RecurringPatternMapper;
import com.adro.tms_backend.repository.RecurringPatternRepository;
import com.adro.tms_backend.repository.TaskRepository;
import com.adro.tms_backend.repository.UserRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RecurringPatternService {

    private final RecurringPatternRepository recurringPatternRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final RecurringPatternMapper recurringPatternMapper;

    @Transactional
    public RecurringPatternDto upsert(UUID taskId, UUID userId, RecurringPatternRequest request) {
        Task task = getOwnedTask(taskId, userId);

        RecurringPattern pattern = recurringPatternRepository
                .findByTaskId(taskId)
                .orElseGet(() -> RecurringPattern.builder().task(task).build());

        recurringPatternMapper.updateEntityFromRequest(request, pattern);

        return recurringPatternMapper.toDto(recurringPatternRepository.save(pattern));
    }

    @Transactional(readOnly = true)
    public RecurringPatternDto getByTask(UUID taskId, UUID userId) {
        getOwnedTask(taskId, userId);
        return recurringPatternMapper.toDto(
                recurringPatternRepository
                        .findByTaskId(taskId)
                        .orElseThrow(() -> ResourceNotFoundException.of("RecurringPattern for task", taskId)));
    }

    @Transactional
    public void delete(UUID taskId, UUID userId) {
        getOwnedTask(taskId, userId);
        recurringPatternRepository
                .findByTaskId(taskId)
                .ifPresent(recurringPatternRepository::delete);
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
