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
 * In-memory caching configuration using Caffeine for analytics computations.
 */
@Configuration
@EnableCaching
@EnableScheduling
@EnableAsync
public class CacheConfig {

    /** Configures Caffeine cache manager with 5-minute expiry and 100-entry limit. */
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
