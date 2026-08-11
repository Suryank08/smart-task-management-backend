package com.adro.tms_backend.service;

import com.adro.tms_backend.entity.Reminder;
import com.adro.tms_backend.entity.Task;
import com.adro.tms_backend.entity.User;
import com.adro.tms_backend.repository.ReminderRepository;
import java.time.Instant;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReminderNotificationService {

    private static final Logger log = LoggerFactory.getLogger(ReminderNotificationService.class);

    private final ReminderRepository reminderRepository;
    private final EmailService emailService;
    private final boolean enabled;

    public ReminderNotificationService(
            ReminderRepository reminderRepository,
            EmailService emailService,
            @Value("${app.mail.reminders.enabled:true}") boolean enabled) {
        this.reminderRepository = reminderRepository;
        this.emailService = emailService;
        this.enabled = enabled;
    }

    @Scheduled(fixedDelayString = "${app.mail.reminders.dispatch-interval-ms:60000}")
    @Transactional
    public void dispatchDueReminders() {
        if (!enabled) {
            return;
        }
        List<Reminder> due = reminderRepository.findDueEmailReminders(Instant.now());
        for (Reminder reminder : due) {
            sendAndMark(reminder);
        }
    }

    private void sendAndMark(Reminder reminder) {
        Task task = reminder.getTask();
        User user = task.getUser();
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            log.warn("Skipping reminder {} — user {} has no email address", reminder.getId(), user.getId());
            return;
        }

        boolean sent = emailService.send(user.getEmail(), buildSubject(task), buildBody(task));
        if (sent) {
            reminder.setSentAt(Instant.now());
            reminderRepository.save(reminder);
        }
    }

    private String buildSubject(Task task) {
        return "Reminder: " + task.getTitle();
    }

    private String buildBody(Task task) {
        StringBuilder body = new StringBuilder();
        body.append("Hi ").append(task.getUser().getName()).append(",\n\n");
        body.append("This is a reminder for your task \"").append(task.getTitle()).append("\".\n");
        if (task.getDueDate() != null) {
            body.append("Due: ").append(task.getDueDate()).append("\n");
        }
        if (task.getDescription() != null && !task.getDescription().isBlank()) {
            body.append("\n").append(task.getDescription()).append("\n");
        }
        body.append("\n— Smart Task Management System");
        return body.toString();
    }
}
