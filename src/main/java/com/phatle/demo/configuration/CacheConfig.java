package com.phatle.demo.configuration;

import java.time.Duration;

import org.springframework.boot.autoconfigure.cache.RedisCacheManagerBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

@Configuration
public class CacheConfig {
	@Bean
	RedisCacheConfiguration cacheConfiguration() {
		return RedisCacheConfiguration.defaultCacheConfig()
				.entryTtl(Duration.ofMinutes(60))
				.disableCachingNullValues()
				.serializeValuesWith(RedisSerializationContext.SerializationPair
						.fromSerializer(new Jackson2JsonRedisSerializer<>(Object.class)));
	}

	@Bean
	RedisCacheManagerBuilderCustomizer redisCacheManagerBuilderCustomizer() {
		return (builder) -> builder
				.withCacheConfiguration("user",
						RedisCacheConfiguration.defaultCacheConfig()
								.entryTtl(Duration.ofMinutes(10)))
				.withCacheConfiguration("users",
						RedisCacheConfiguration.defaultCacheConfig()
								.entryTtl(Duration.ofMinutes(5)))
				.withCacheConfiguration("file",
						RedisCacheConfiguration.defaultCacheConfig()
								.entryTtl(Duration.ofMinutes(15)));
	}
}