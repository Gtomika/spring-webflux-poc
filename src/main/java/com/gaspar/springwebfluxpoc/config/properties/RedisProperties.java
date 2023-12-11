package com.gaspar.springwebfluxpoc.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "cache.redis")
public record RedisProperties(
        String host,
        Integer port,
        String password,
        Integer ttlMinutes
) {}
