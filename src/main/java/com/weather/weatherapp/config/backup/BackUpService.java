package com.weather.weatherapp.config.backup;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.metamodel.EntityType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
@Transactional
public class BackUpService {
    private final EntityManager entityManager;
    private final BackUpRepository backupRepository;
    private final ObjectMapper objectMapper;

    public BackUpService(EntityManager entityManager, BackUpRepository backupRepository, ObjectMapper objectMapper) {
        this.entityManager = entityManager;
        this.backupRepository = backupRepository;
        this.objectMapper = objectMapper;
    }


    public void backupAllData() {
        Set<EntityType<?>> entities = entityManager.getMetamodel().getEntities();
        LocalDateTime now = LocalDateTime.now();

        for (EntityType<?> entityType : entities) {
            String entityName = entityType.getName();
            List<?> allEntities = entityManager.createQuery("SELECT e FROM " + entityName + " e").getResultList();

            for (Object entity : allEntities) {
                try {
                    String entityId = entityManager.getEntityManagerFactory()
                            .getPersistenceUnitUtil().getIdentifier(entity).toString();
                    String entityData = objectMapper.writeValueAsString(entity);

                    BackUpEntity backupEntry = new BackUpEntity();
                    backupEntry.setEntityName(entityName);
                    backupEntry.setEntityId(entityId);
                    backupEntry.setEntityData(entityData);
                    backupEntry.setBackupTime(now);

                    backupRepository.save(backupEntry);
                } catch (JsonProcessingException e) {
                    // Log error
                }
            }
        }
    }

    public void restoreData(LocalDateTime backupTime) {
        Set<EntityType<?>> entities = entityManager.getMetamodel().getEntities();

        for (EntityType<?> entityType : entities) {
            String entityName = entityType.getName();
            List<BackUpEntity> backupEntries = backupRepository.findByEntityNameAndAndBackupTimeBetween(
                    entityName, backupTime, backupTime.plusSeconds(1));

            for (BackUpEntity backupEntry : backupEntries) {
                try {
                    Object entity = objectMapper.readValue(backupEntry.getEntityData(), entityType.getJavaType());
                    entityManager.merge(entity);
                } catch (JsonProcessingException e) {
                    // Log error
                }
            }
        }
    }
}
