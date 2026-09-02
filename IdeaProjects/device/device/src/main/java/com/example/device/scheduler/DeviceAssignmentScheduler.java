package com.example.device.scheduler;

import com.example.device.service.AssignmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DeviceAssignmentScheduler {

    private final AssignmentService assignmentService;

    @Scheduled(cron = "${app.scheduler.overdue-cron}", zone = "${app.scheduler.zone}")
    public void processAssignments() {
        int reminders = assignmentService.sendUpcomingDueNotifications();
        int overdue = assignmentService.updateOverdueAssignments();
        int overdueEmails = assignmentService.sendOverdueNotifications();

        if (reminders > 0 || overdue > 0 || overdueEmails > 0) {
            log.info("Reminders: {}, overdue: {}, overdue emails: {}",
                    reminders, overdue, overdueEmails);
        }
    }

    @Scheduled(cron = "${app.scheduler.daily-overdue-report-cron}", zone = "${app.scheduler.zone}")
    public void sendDailyOverdueReport() {
        int sent = assignmentService.sendDailyOverdueSummary();

        if (sent > 0) {
            log.info("Daily overdue report sent to {} recipients", sent);
        }
    }
}