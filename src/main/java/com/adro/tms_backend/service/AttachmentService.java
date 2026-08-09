package com.adro.tms_backend.service;

import com.adro.tms_backend.dto.AttachmentCreateRequest;
import com.adro.tms_backend.dto.AttachmentDto;
import com.adro.tms_backend.entity.Attachment;
import com.adro.tms_backend.entity.Task;
import com.adro.tms_backend.entity.User;
import com.adro.tms_backend.entity.enums.UserStatus;
import com.adro.tms_backend.exception.ResourceNotFoundException;
import com.adro.tms_backend.mapper.AttachmentMapper;
import com.adro.tms_backend.repository.AttachmentRepository;
import com.adro.tms_backend.repository.TaskRepository;
import com.adro.tms_backend.repository.UserRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AttachmentService {

    private final AttachmentRepository attachmentRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final AttachmentMapper attachmentMapper;

    @Transactional
    public AttachmentDto create(UUID taskId, UUID userId, AttachmentCreateRequest request) {
        Task task = getOwnedTask(taskId, userId);
        User user = userRepository
                .findByIdAndStatus(userId, UserStatus.ACTIVE)
                .orElseThrow(() -> ResourceNotFoundException.of("User", userId));

        Attachment attachment = Attachment.builder()
                .task(task)
                .user(user)
                .fileName(request.fileName())
                .fileUrl(request.fileUrl())
                .mimeType(request.mimeType())
                .fileSizeKb(request.fileSizeKb())
                .build();

        return attachmentMapper.toDto(attachmentRepository.save(attachment));
    }

    @Transactional(readOnly = true)
    public List<AttachmentDto> listByTask(UUID taskId, UUID userId) {
        getOwnedTask(taskId, userId);
        return attachmentMapper.toDtoList(attachmentRepository.findByTaskIdOrderByUploadedAtDesc(taskId));
    }

    @Transactional
    public void delete(UUID id, UUID taskId, UUID userId) {
        getOwnedTask(taskId, userId);
        Attachment attachment = attachmentRepository
                .findByIdAndTaskId(id, taskId)
                .orElseThrow(() -> ResourceNotFoundException.of("Attachment", id));
        attachmentRepository.delete(attachment);
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
