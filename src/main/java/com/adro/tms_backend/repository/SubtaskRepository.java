package com.adro.tms_backend.repository;

import com.adro.tms_backend.entity.Subtask;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubtaskRepository extends JpaRepository<Subtask, UUID> {

    List<Subtask> findByTaskIdOrderByPositionAsc(UUID taskId);

    Optional<Subtask> findByIdAndTaskId(UUID id, UUID taskId);
}
