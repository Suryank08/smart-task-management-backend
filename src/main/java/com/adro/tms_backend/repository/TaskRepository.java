package com.adro.tms_backend.repository;

import com.adro.tms_backend.entity.Task;
import com.adro.tms_backend.entity.enums.TaskStatus;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface TaskRepository extends JpaRepository<Task, UUID>, JpaSpecificationExecutor<Task> {

    Optional<Task> findByIdAndDeletedAtIsNull(UUID id);

    List<Task> findByUserIdAndStatusInAndDeletedAtIsNull(UUID userId, Collection<TaskStatus> statuses);
}
