package com.campuspe.soc2_readiness_manager.config;

import java.time.Duration;
import java.util.Set;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@EnableCaching
public class CacheConfig {

    public static final String READINESS_ITEM_BY_ID_CACHE = "readiness-item-by-id";
    public static final String READINESS_ITEMS_PAGE_CACHE = "readiness-items-page";
    public static final String READINESS_ITEMS_LIST_CACHE = "readiness-items-list";

    private static final Duration CACHE_TTL = Duration.ofMinutes(10);

    @Bean
    @SuppressWarnings("deprecation")
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration defaultConfiguration = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(CACHE_TTL)
                .disableCachingNullValues()
                .serializeKeysWith(SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfiguration)
                .initialCacheNames(Set.of(
                        READINESS_ITEM_BY_ID_CACHE,
                        READINESS_ITEMS_PAGE_CACHE,
                        READINESS_ITEMS_LIST_CACHE))
                .build();
    }
}
