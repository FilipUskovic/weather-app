package com.weather.weatherapp.config.backup;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/backup")
public class BackUpController {

    private final BackUpService backupService;


    public BackUpController(BackUpService backupService) {
        this.backupService = backupService;
    }

    @PostMapping("/create")
    public ResponseEntity<String> createBackup() {
        try {
            backupService.backupAllData();
            return ResponseEntity.ok("Backup created successfully");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to create backup: " + e.getMessage());
        }
    }

    @PostMapping("/restore")
    public ResponseEntity<String> restoreBackup(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime backupTime) {
        try {
            backupService.restoreData(backupTime);
            return ResponseEntity.ok("Database restored successfully to state at " + backupTime);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to restore backup: " + e.getMessage());
        }
    }
}
