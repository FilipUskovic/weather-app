package com.weather.weatherapp.config.backup;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BackUpRepository extends JpaRepository<BackUpEntity, Long> {

    List<BackUpEntity> findByEntityNameAndAndBackupTimeBetween(String entityName, LocalDateTime start, LocalDateTime end);
}
