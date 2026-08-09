package com.adro.tms_backend.repository;

import com.adro.tms_backend.entity.Reminder;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReminderRepository extends JpaRepository<Reminder, UUID> {

    List<Reminder> findByTaskIdOrderByRemindAtAsc(UUID taskId);

    Optional<Reminder> findByIdAndTaskId(UUID id, UUID taskId);

    List<Reminder> findBySentAtIsNullAndRemindAtLessThanEqual(Instant now);
}
