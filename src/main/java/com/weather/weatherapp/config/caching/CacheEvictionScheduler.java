package com.weather.weatherapp.config.caching;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.CacheManager;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.util.Objects;

@Component
public class CacheEvictionScheduler {

    private static final Logger log = LoggerFactory.getLogger(CacheEvictionScheduler.class);
    private final CacheManager cacheManager;


    public CacheEvictionScheduler(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    @Scheduled(fixedRate = 3600000)
    public void evictAllCaches() {
        cacheManager.getCacheNames()
                .forEach(cacheName -> Objects.requireNonNull(cacheManager.getCache(cacheName)).clear());
    }

    public void evictSpecificCache(String cacheName) {
        Objects.requireNonNull(cacheManager.getCache(cacheName)).clear();
        log.info("Cleared specific cache: {}", cacheName);
    }


}
