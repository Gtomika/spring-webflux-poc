package com.gaspar.springwebfluxpoc.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "external-api")
public record ExternalApiProperties(
        String url,
        String userAgent,
        String apiKey,
        Integer timeoutMillis,
        String userPath,
        String reservationsPath,
        String paymentHistoryPath
) {}
