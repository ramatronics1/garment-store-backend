package com.garmentstore.common.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
@RequiredArgsConstructor
public class RateLimiterService {
    private final RateLimitProperties properties;
    private final ConcurrentHashMap<String, WindowCounter> requestCounts = new ConcurrentHashMap<>();

    public boolean isAllowed(String ipAddress, String endpointKey, int maxRequestsPerMinute) {
        if (!properties.enabled()) {
            return true;
        }

        String safeIp = (ipAddress == null || ipAddress.isBlank()) ? "unknown" : ipAddress.trim();
        String key = endpointKey + ":" + safeIp;
        long currentMinute = System.currentTimeMillis() / 60000;

        // Lightweight cleanup when map exceeds 5000 items
        if (requestCounts.size() > 5000) {
            requestCounts.entrySet().removeIf(entry -> entry.getValue().minuteWindow < currentMinute - 2);
        }

        WindowCounter counter = requestCounts.compute(key, (k, existing) -> {
            if (existing == null || existing.minuteWindow != currentMinute) {
                return new WindowCounter(currentMinute, new AtomicInteger(1));
            }
            existing.count.incrementAndGet();
            return existing;
        });

        int currentCount = counter.count.get();
        if (currentCount > maxRequestsPerMinute) {
            log.warn("Rate limit exceeded for IP [{}] on [{}]. Attempts this minute: {} (Limit: {})",
                    safeIp, endpointKey, currentCount, maxRequestsPerMinute);
            return false;
        }

        return true;
    }

    private static class WindowCounter {
        final long minuteWindow;
        final AtomicInteger count;

        WindowCounter(long minuteWindow, AtomicInteger count) {
            this.minuteWindow = minuteWindow;
            this.count = count;
        }
    }
}
