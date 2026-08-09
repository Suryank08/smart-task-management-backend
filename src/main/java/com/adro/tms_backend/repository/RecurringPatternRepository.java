package com.adro.tms_backend.repository;

import com.adro.tms_backend.entity.RecurringPattern;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecurringPatternRepository extends JpaRepository<RecurringPattern, UUID> {

    Optional<RecurringPattern> findByTaskId(UUID taskId);
}
