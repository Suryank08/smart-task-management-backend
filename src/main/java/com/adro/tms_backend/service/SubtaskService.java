package com.adro.tms_backend.service;

import com.adro.tms_backend.dto.SubtaskDto;
import com.adro.tms_backend.dto.SubtaskRequest;
import com.adro.tms_backend.entity.Subtask;
import com.adro.tms_backend.entity.Task;
import com.adro.tms_backend.entity.enums.UserStatus;
import com.adro.tms_backend.exception.ResourceNotFoundException;
import com.adro.tms_backend.mapper.SubtaskMapper;
import com.adro.tms_backend.repository.SubtaskRepository;
import com.adro.tms_backend.repository.TaskRepository;
import com.adro.tms_backend.repository.UserRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SubtaskService {

    private final SubtaskRepository subtaskRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final SubtaskMapper subtaskMapper;

    @Transactional
    public SubtaskDto create(UUID taskId, UUID userId, SubtaskRequest request) {
        Task task = getOwnedTask(taskId, userId);

        Subtask subtask = Subtask.builder()
                .task(task)
                .title(request.title())
                .completed(request.completed())
                .position(request.position())
                .build();

        return subtaskMapper.toDto(subtaskRepository.save(subtask));
    }

    @Transactional(readOnly = true)
    public List<SubtaskDto> listByTask(UUID taskId, UUID userId) {
        getOwnedTask(taskId, userId);
        return subtaskMapper.toDtoList(subtaskRepository.findByTaskIdOrderByPositionAsc(taskId));
    }

    @Transactional
    public SubtaskDto update(UUID id, UUID taskId, UUID userId, SubtaskRequest request) {
        getOwnedTask(taskId, userId);
        Subtask subtask = getSubtask(id, taskId);
        subtaskMapper.updateEntityFromRequest(request, subtask);
        return subtaskMapper.toDto(subtaskRepository.save(subtask));
    }

    @Transactional
    public void delete(UUID id, UUID taskId, UUID userId) {
        getOwnedTask(taskId, userId);
        subtaskRepository.delete(getSubtask(id, taskId));
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

    private Subtask getSubtask(UUID id, UUID taskId) {
        return subtaskRepository
                .findByIdAndTaskId(id, taskId)
                .orElseThrow(() -> ResourceNotFoundException.of("Subtask", id));
    }
}
