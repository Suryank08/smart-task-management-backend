package com.adro.tms_backend.service;

import com.adro.tms_backend.dto.ReminderDto;
import com.adro.tms_backend.dto.ReminderRequest;
import com.adro.tms_backend.entity.Reminder;
import com.adro.tms_backend.entity.Task;
import com.adro.tms_backend.entity.enums.UserStatus;
import com.adro.tms_backend.exception.ResourceNotFoundException;
import com.adro.tms_backend.mapper.ReminderMapper;
import com.adro.tms_backend.repository.ReminderRepository;
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
public class ReminderService {

    private final ReminderRepository reminderRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final ReminderMapper reminderMapper;

    @Transactional
    public ReminderDto create(UUID taskId, UUID userId, ReminderRequest request) {
        Task task = getOwnedTask(taskId, userId);

        Reminder reminder = Reminder.builder()
                .task(task)
                .remindAt(request.remindAt())
                .channel(request.channel() != null ? request.channel() : "EMAIL")
                .build();

        return reminderMapper.toDto(reminderRepository.save(reminder));
    }

    @Transactional(readOnly = true)
    public List<ReminderDto> listByTask(UUID taskId, UUID userId) {
        getOwnedTask(taskId, userId);
        return reminderMapper.toDtoList(reminderRepository.findByTaskIdOrderByRemindAtAsc(taskId));
    }

    @Transactional
    public ReminderDto update(UUID id, UUID taskId, UUID userId, ReminderRequest request) {
        getOwnedTask(taskId, userId);
        Reminder reminder = getReminder(id, taskId);
        reminderMapper.updateEntityFromRequest(request, reminder);
        return reminderMapper.toDto(reminderRepository.save(reminder));
    }

    @Transactional
    public void delete(UUID id, UUID taskId, UUID userId) {
        getOwnedTask(taskId, userId);
        reminderRepository.delete(getReminder(id, taskId));
    }

    @Transactional
    public void markSent(UUID id, UUID taskId, UUID userId) {
        getOwnedTask(taskId, userId);
        Reminder reminder = getReminder(id, taskId);
        reminder.setSentAt(Instant.now());
        reminderRepository.save(reminder);
    }

    @Transactional(readOnly = true)
    public List<ReminderDto> findDue() {
        return reminderMapper.toDtoList(reminderRepository.findBySentAtIsNullAndRemindAtLessThanEqual(Instant.now()));
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

    private Reminder getReminder(UUID id, UUID taskId) {
        return reminderRepository
                .findByIdAndTaskId(id, taskId)
                .orElseThrow(() -> ResourceNotFoundException.of("Reminder", id));
    }
}
