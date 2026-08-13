package com.adro.tms_backend.service;

import com.adro.tms_backend.dto.TaskCreateRequest;
import com.adro.tms_backend.dto.TaskDto;
import com.adro.tms_backend.dto.TaskUpdateRequest;
import com.adro.tms_backend.entity.Category;
import com.adro.tms_backend.entity.Tag;
import com.adro.tms_backend.entity.Task;
import com.adro.tms_backend.entity.User;
import com.adro.tms_backend.entity.enums.TaskPriority;
import com.adro.tms_backend.entity.enums.TaskStatus;
import com.adro.tms_backend.entity.enums.UserStatus;
import com.adro.tms_backend.exception.ResourceNotFoundException;
import com.adro.tms_backend.mapper.TaskMapper;
import com.adro.tms_backend.repository.CategoryRepository;
import com.adro.tms_backend.repository.TagRepository;
import com.adro.tms_backend.repository.TaskRepository;
import com.adro.tms_backend.repository.TaskSpecifications;
import com.adro.tms_backend.repository.UserRepository;
import com.adro.tms_backend.repository.ReminderRepository;
import com.adro.tms_backend.repository.SubtaskRepository;
import com.adro.tms_backend.entity.Reminder;
import com.adro.tms_backend.entity.Subtask;
import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;
    private final ReminderRepository reminderRepository;
    private final SubtaskRepository subtaskRepository;
    private final TaskMapper taskMapper;

    @Transactional
    public TaskDto create(UUID userId, TaskCreateRequest request) {
        User user = userRepository
                .findByIdAndStatus(userId, UserStatus.ACTIVE)
                .orElseThrow(() -> ResourceNotFoundException.of("User", userId));

        Task task = Task.builder()
                .user(user)
                .category(resolveOwnedCategory(request.categoryId(), userId))
                .title(request.title())
                .description(request.description())
                .priority(request.priority() != null ? request.priority() : TaskPriority.MEDIUM)
                .startDate(request.startDate())
                .dueDate(request.dueDate())
                .estimatedMinutes(request.estimatedMinutes())
                .reminderAt(request.reminderAt())
                .pinned(request.pinned() != null ? request.pinned() : false)
                .tags(resolveOwnedTags(request.tagIds(), userId))
                .build();

        Task saved = taskRepository.save(task);
        updateTaskReminders(saved, request.reminderAt());

        if (request.subtasks() != null && !request.subtasks().isEmpty()) {
            int position = 0;
            for (String subtaskTitle : request.subtasks()) {
                if (subtaskTitle != null && !subtaskTitle.isBlank()) {
                    Subtask subtask = Subtask.builder()
                            .task(saved)
                            .title(subtaskTitle.trim())
                            .completed(false)
                            .position(position++)
                            .build();
                    subtaskRepository.save(subtask);
                }
            }
        }

        return taskMapper.toDto(saved);
    }

    @Transactional(readOnly = true)
    public TaskDto findById(UUID id, UUID userId) {
        return taskMapper.toDto(getOwnedTask(id, userId));
    }

    @Transactional(readOnly = true)
    public Page<TaskDto> search(
            UUID userId,
            TaskStatus status,
            TaskPriority priority,
            Boolean pinned,
            Instant dueFrom,
            Instant dueTo,
            String search,
            Pageable pageable) {
        requireActiveUser(userId);
        Specification<Task> spec =
                TaskSpecifications.filter(userId, status, priority, pinned, dueFrom, dueTo, search);
        return taskRepository.findAll(spec, pageable).map(taskMapper::toDto);
    }

    @Transactional
    public TaskDto update(UUID id, UUID userId, TaskUpdateRequest request) {
        Task task = getOwnedTask(id, userId);

        TaskStatus previousStatus = task.getStatus();
        taskMapper.updateEntityFromRequest(request, task);
        task.setCategory(resolveOwnedCategory(request.categoryId(), userId));
        task.setTags(resolveOwnedTags(request.tagIds(), userId));

        if (request.status() != null && request.status() != previousStatus) {
            task.setCompletedAt(request.status() == TaskStatus.COMPLETED ? Instant.now() : null);
        }

        Task saved = taskRepository.save(task);

        if (saved.getStatus() == TaskStatus.COMPLETED || saved.getStatus() == TaskStatus.CANCELLED) {
            reminderRepository.deleteByTaskIdAndSentAtIsNull(saved.getId());
        } else {
            updateTaskReminders(saved, request.reminderAt());
        }

        return taskMapper.toDto(saved);
    }

    @Transactional
    public void softDelete(UUID id, UUID userId) {
        Task task = getOwnedTask(id, userId);
        task.setDeletedAt(Instant.now());
        taskRepository.save(task);
        reminderRepository.deleteByTaskIdAndSentAtIsNull(task.getId());
    }

    private Task getOwnedTask(UUID id, UUID userId) {
        requireActiveUser(userId);
        Task task = taskRepository
                .findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Task", id));
        if (!task.getUser().getId().equals(userId)) {
            throw ResourceNotFoundException.of("Task", id);
        }
        return task;
    }

    private void requireActiveUser(UUID userId) {
        if (!userRepository.existsByIdAndStatus(userId, UserStatus.ACTIVE)) {
            throw ResourceNotFoundException.of("User", userId);
        }
    }

    private Category resolveOwnedCategory(UUID categoryId, UUID userId) {
        if (categoryId == null) {
            return null;
        }
        Category category = categoryRepository
                .findById(categoryId)
                .orElseThrow(() -> ResourceNotFoundException.of("Category", categoryId));
        if (!category.getUser().getId().equals(userId)) {
            throw ResourceNotFoundException.of("Category", categoryId);
        }
        return category;
    }

    private Set<Tag> resolveOwnedTags(Set<UUID> tagIds, UUID userId) {
        if (tagIds == null || tagIds.isEmpty()) {
            return new HashSet<>();
        }
        Set<Tag> tags = new HashSet<>(tagRepository.findAllById(tagIds));
        if (tags.size() != tagIds.size()) {
            throw new ResourceNotFoundException("One or more tags were not found");
        }
        for (Tag tag : tags) {
            if (!tag.getUser().getId().equals(userId)) {
                throw ResourceNotFoundException.of("Tag", tag.getId());
            }
        }
        return tags;
    }

    private void updateTaskReminders(Task task, Instant reminderAt) {
        reminderRepository.deleteByTaskIdAndSentAtIsNull(task.getId());
        if (reminderAt != null) {
            Reminder primary = Reminder.builder()
                    .task(task)
                    .remindAt(reminderAt)
                    .channel("EMAIL")
                    .build();
            reminderRepository.save(primary);

            if (task.getDueDate() != null && task.getEstimatedMinutes() != null) {
                Instant limitTime = task.getDueDate().minus(Duration.ofMinutes(task.getEstimatedMinutes() + 60));
                if (reminderAt.isBefore(limitTime)) {
                    Reminder secondary = Reminder.builder()
                            .task(task)
                            .remindAt(limitTime)
                            .channel("EMAIL")
                            .build();
                    reminderRepository.save(secondary);
                }
            }
        }
    }
}
