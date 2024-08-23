package com.weather.weatherapp.config.backup;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/backup")
public class BackUpController {

    private static final Logger log = LoggerFactory.getLogger(BackUpController.class);
    private final BackUpService backupService;


    public BackUpController(BackUpService backupService) {
        this.backupService = backupService;
    }

    @PostMapping("/create")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<String> createBackup() {
        try {

            backupService.backupAllData();
            return ResponseEntity.ok("Backup created successfully");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to create backup: " + e.getMessage());
        }
    }

    @PostMapping("/restore")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<String> restoreBackup(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime backupTime) {
        try {
            backupService.restoreData(backupTime);
            return ResponseEntity.ok("Database restored successfully to state at " + backupTime);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to restore backup: " + e.getMessage());
        }
    }



}
