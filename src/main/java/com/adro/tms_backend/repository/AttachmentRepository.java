package com.adro.tms_backend.repository;

import com.adro.tms_backend.entity.Attachment;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttachmentRepository extends JpaRepository<Attachment, UUID> {

    List<Attachment> findByTaskIdOrderByUploadedAtDesc(UUID taskId);

    Optional<Attachment> findByIdAndTaskId(UUID id, UUID taskId);
}
