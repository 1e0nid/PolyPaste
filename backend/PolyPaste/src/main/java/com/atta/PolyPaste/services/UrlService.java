package com.atta.PolyPaste.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UrlService {

    private final StringRedisTemplate redisTemplate;
    private static final String URL_POOL_KEY = "available_urls";
    private static final String RESERVED_PREFIX = "url:reserved:";

    public String getFreeUrl() {
        int maxAttempts = 10;

        for (int attempt = 0; attempt < maxAttempts; attempt++) {
            String url = redisTemplate.opsForSet().pop(URL_POOL_KEY);

            if (url == null) {
                url = UUID.randomUUID().toString().substring(0, 8);
            }

            Boolean isUnique = redisTemplate.opsForValue()
                    .setIfAbsent(RESERVED_PREFIX + url, "occupied", Duration.ofDays(7));

            if (Boolean.TRUE.equals(isUnique)) {
                return url;
            }
            log.warn("Обнаружена коллизия URL: [{}]. Повторная генерация...", url);
        }

        throw new RuntimeException("Не удалось сгенерировать уникальный URL после " + maxAttempts + " попыток");
    }
}