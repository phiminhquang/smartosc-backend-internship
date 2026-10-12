package com.example.device.scheduler;

import com.example.device.observability.OperationalMetrics;
import com.example.device.service.AssignmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.scheduler.enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
@Slf4j
public class DeviceAssignmentScheduler {

    private final AssignmentService assignmentService;
    private final OperationalMetrics metrics;

    @Scheduled(cron = "${app.scheduler.overdue-cron}", zone = "${app.scheduler.zone}")
    public void processAssignments() {
        metrics.recordSchedulerRun("assignment_notifications", () -> {
            int reminders = assignmentService.sendUpcomingDueNotifications();
            int overdue = assignmentService.updateOverdueAssignments();
            int overdueEmails = assignmentService.sendOverdueNotifications();

            if (reminders > 0 || overdue > 0 || overdueEmails > 0) {
                log.info("event=assignment_notification_batch reminder_jobs={} overdue_assignments={} overdue_email_jobs={}",
                        reminders, overdue, overdueEmails);
            }
        });
    }

    @Scheduled(cron = "${app.scheduler.daily-overdue-report-cron}", zone = "${app.scheduler.zone}")
    public void sendDailyOverdueReport() {
        metrics.recordSchedulerRun("daily_overdue_report", () -> {
            int enqueued = assignmentService.sendDailyOverdueSummary();

            if (enqueued > 0) {
                log.info("event=daily_overdue_report_enqueued recipient_count={}", enqueued);
            }
        });
    }
}
