package com.bankingoop.finance.config;

import java.time.Duration;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.github.benmanes.caffeine.cache.Caffeine;

/**
 * Cache configuration using Caffeine — high-performance in-memory caching.
 *
 * Design decision — cache analytics, not transactions:
 *   We cache expensive analytics computations (monthly trends, anomaly detection)
 *   that read all transactions but change infrequently. Transaction CRUD operations
 *   evict the relevant caches to ensure consistency.
 *
 *   Caffeine was chosen over simple ConcurrentHashMap because it provides:
 *   - Size-based eviction (maximumSize)
 *   - Time-based expiration (expireAfterWrite)
 *   - Statistics recording for monitoring
 */
@Configuration
@EnableCaching
@EnableScheduling
@EnableAsync
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(
                "monthlyTrends", "topCategories", "savingsRate",
                "budgetStatuses", "recurringTransactions"
        );
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .maximumSize(100)
                .expireAfterWrite(Duration.ofMinutes(5))
                .recordStats());
        return cacheManager;
    }
}
