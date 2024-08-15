package com.weather.weatherapp.config;

import org.springframework.cache.CacheManager;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class CacheEvictionScheduler {
    // Posto SimpleChahce nema ugraden automatsku istjecanje cache-a moramo sami paziti na to
    // mogao sam Koristi Caffeine npr za chace koji ima ugraden no nisam htio dodavati 3-party dependecije

    private final CacheManager cacheManager;


    public CacheEvictionScheduler(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    @Scheduled(fixedRate = 3600000) // Svakih sat vremena
    public void evictAllCaches() {
        cacheManager.getCacheNames().stream()
                .forEach(cacheName -> cacheManager.getCache(cacheName).clear());
    }


}
