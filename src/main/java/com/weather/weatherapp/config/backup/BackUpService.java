package com.weather.weatherapp.config.backup;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.metamodel.EntityType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
@Transactional
public class BackUpService {
    private static final Logger log = LoggerFactory.getLogger(BackUpService.class);
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
//
                }
            }
        }
    }

    private void backupEntityStream(EntityType<?> entityType, LocalDateTime backupTime) {
        String entityName = entityType.getName();
        try {
            List<?> allEntities = entityManager.createQuery("SELECT e FROM " + entityName + " e").getResultList();
            allEntities.forEach(entity -> backupSingleEntity(entity, entityName, backupTime));
        } catch (Exception e) {
            log.error("Error backing up entity type: {}", entityName, e);
        }
    }

    private void backupSingleEntity(Object entity, String entityName, LocalDateTime backupTime) {
        try {
            String entityId = entityManager.getEntityManagerFactory()
                    .getPersistenceUnitUtil().getIdentifier(entity).toString();
            String entityData = objectMapper.writeValueAsString(entity);

            BackUpEntity backupEntry = new BackUpEntity();
            backupEntry.setEntityName(entityName);
            backupEntry.setEntityId(entityId);
            backupEntry.setEntityData(entityData);
            backupEntry.setBackupTime(backupTime);

            backupRepository.save(backupEntry);
        } catch (JsonProcessingException e) {
            log.error("Error backing up entity: {} with ID: {}", entityName, entity, e);
        }
    }


    public void restoreData(LocalDateTime backupTime) {
        if (backupTime.isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("Ne možete vratiti podatke iz budućnosti.");
        }

        Set<EntityType<?>> entities = entityManager.getMetamodel().getEntities();

        for (EntityType<?> entityType : entities) {
            String entityName = entityType.getName();
            try {
                List<BackUpEntity> backupEntries = backupRepository.findByEntityNameAndAndBackupTimeBetween(
                        entityName, backupTime, backupTime.plusSeconds(1));

                if (backupEntries.isEmpty()) {
                    log.warn("Nema backup podataka za entitet {} na vrijeme {}", entityName, backupTime);
                    continue;
                }

                for (BackUpEntity backupEntry : backupEntries) {
                    restoreSingleEntity(backupEntry, entityType);
                }
            } catch (Exception e) {
                log.error("Greška prilikom vraćanja podataka za tip entiteta: {}", entityName, e);
            }
        }
    }

    protected void restoreSingleEntity(BackUpEntity backupEntry, EntityType<?> entityType) {
        try {
            Object entity = objectMapper.readValue(backupEntry.getEntityData(), entityType.getJavaType());
            entityManager.merge(entity);
        } catch (JsonProcessingException e) {
            log.error("Error restoring entity: {} with ID: {}", backupEntry.getEntityName(), backupEntry.getEntityId(), e);
        }
    }

    /* staro radiii

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

                }
            }
        }
    }

     */
}
