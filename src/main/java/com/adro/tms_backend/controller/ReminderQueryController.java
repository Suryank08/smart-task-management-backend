package com.adro.tms_backend.controller;

import com.adro.tms_backend.dto.ReminderDto;
import com.adro.tms_backend.service.ReminderService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reminders")
@RequiredArgsConstructor
public class ReminderQueryController {

    private final ReminderService reminderService;

    @GetMapping("/due")
    public List<ReminderDto> findDue() {
        return reminderService.findDue();
    }
}
