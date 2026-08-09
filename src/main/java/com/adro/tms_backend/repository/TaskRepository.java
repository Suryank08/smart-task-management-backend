package com.adro.tms_backend.repository;

import com.adro.tms_backend.entity.Task;
import com.adro.tms_backend.entity.enums.TaskPriority;
import com.adro.tms_backend.entity.enums.TaskStatus;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TaskRepository extends JpaRepository<Task, UUID> {

    Optional<Task> findByIdAndDeletedAtIsNull(UUID id);

    Page<Task> findByUserIdAndDeletedAtIsNull(UUID userId, Pageable pageable);

    Page<Task> findByUserIdAndStatusAndDeletedAtIsNull(UUID userId, TaskStatus status, Pageable pageable);

    Page<Task> findByUserIdAndPriorityAndDeletedAtIsNull(UUID userId, TaskPriority priority, Pageable pageable);

    Page<Task> findByUserIdAndArchivedAndDeletedAtIsNull(UUID userId, boolean archived, Pageable pageable);

    Page<Task> findByUserIdAndDueDateBetweenAndDeletedAtIsNull(
            UUID userId, Instant from, Instant to, Pageable pageable);

    @Query(
            value = "SELECT * FROM tasks WHERE user_id = :userId AND deleted_at IS NULL "
                    + "AND title % :query ORDER BY similarity(title, :query) DESC",
            nativeQuery = true)
    Page<Task> searchByTitle(@Param("userId") UUID userId, @Param("query") String query, Pageable pageable);
}
