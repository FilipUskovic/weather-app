package com.weather.weatherapp.config.caching;

import com.weather.weatherapp.weatherForecast.Predictions.dto.TemperatureTrend;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;
@Service
public class CachingRedisService {

    private final RedisTemplate<String, Object> redisTemplate;

    public CachingRedisService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }


    public void cacheData(String key, Object data, long ttlInSeconds) {
        redisTemplate.opsForValue().set(key, data, ttlInSeconds, TimeUnit.SECONDS);
    }

    public Object getCachedData(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    public void deleteCachedData(String key) {
        redisTemplate.delete(key);
    }
}
