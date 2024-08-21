package com.weather.weatherapp.config.backup;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ScheduledBackup {
    private final BackUpService backupService;

    public ScheduledBackup(BackUpService backupService) {
        this.backupService = backupService;
    }

    @Scheduled(cron = "0 0 1 * * ?") // Svaki dan u 1:00
    public void performDailyBackup() {
        backupService.backupAllData();
    }
}
