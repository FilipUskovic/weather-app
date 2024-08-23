package com.weather.weatherapp.config.caching;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
public class CachingRedisService {
    private static final Logger log = LoggerFactory.getLogger(CachingRedisService.class);
    private final RedisTemplate<String, Object> redisTemplate;

    public CachingRedisService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void cacheData(String key, Object data, long ttlInSeconds) {
        try {
            redisTemplate.opsForValue().set(key, data, ttlInSeconds, TimeUnit.SECONDS);
            log.info("Podaci uspješno spremljeni u cache s ključem: {}", key);
        } catch (Exception e) {
            log.error("Greška pri spremanju podataka u cache s ključem: {}", key, e);
        }
    }

    public Object getCachedData(String key) {
        try {
            Object data = redisTemplate.opsForValue().get(key);
            if (data != null) {
                log.info("Podaci uspješno dohvaćeni iz cachea s ključem: {}", key);
            } else {
                log.info("Nema podataka u cacheu za ključ: {}", key);
            }
            return data;
        } catch (Exception e) {
            log.error("Greška pri dohvaćanju podataka iz cachea s ključem: {}", key, e);
            return null;
        }
    }

    public void deleteCachedData(String key) {
        try {
            Boolean result = redisTemplate.delete(key);
            if (Boolean.TRUE.equals(result)) {
                log.info("Podaci uspješno obrisani iz cachea s ključem: {}", key);
            } else {
                log.info("Nema podataka za brisanje u cacheu s ključem: {}", key);
            }
        } catch (Exception e) {
            log.error("Greška pri brisanju podataka iz cachea s ključem: {}", key, e);
        }
    }

    public void deleteAllCachedData(String pattern) {
        try {
            Set<String> keys = redisTemplate.keys(pattern + "*");
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
                log.info("Uspješno obrisani svi podaci iz cachea s uzorkom: {}", pattern);
            } else {
                log.info("Nema podataka za brisanje u cacheu s uzorkom: {}", pattern);
            }
        } catch (Exception e) {
            log.error("Greška pri brisanju svih podataka iz cachea s uzorkom: {}", pattern, e);
        }
    }

    public boolean hasKey(String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }


    public void setExpire(String key, long ttlInSeconds) {
        redisTemplate.expire(key, ttlInSeconds, TimeUnit.SECONDS);
    }

}
