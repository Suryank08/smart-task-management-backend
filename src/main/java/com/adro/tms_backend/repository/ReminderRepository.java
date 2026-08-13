package com.adro.tms_backend.repository;

import com.adro.tms_backend.entity.Reminder;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReminderRepository extends JpaRepository<Reminder, UUID> {

    List<Reminder> findByTaskIdOrderByRemindAtAsc(UUID taskId);

    Optional<Reminder> findByIdAndTaskId(UUID id, UUID taskId);

    List<Reminder> findBySentAtIsNullAndRemindAtLessThanEqual(Instant now);

    @Query("select r from Reminder r join fetch r.task t join fetch t.user "
            + "where r.sentAt is null and r.remindAt <= :now and upper(r.channel) = 'EMAIL' "
            + "and t.deletedAt is null")
    List<Reminder> findDueEmailReminders(@Param("now") Instant now);

    void deleteByTaskIdAndSentAtIsNull(UUID taskId);
}
