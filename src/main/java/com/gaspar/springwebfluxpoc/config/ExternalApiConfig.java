package com.gaspar.springwebfluxpoc.config;

import com.gaspar.springwebfluxpoc.config.properties.ExternalApiProperties;
import com.gaspar.springwebfluxpoc.exception.ExternalApiException;
import io.netty.channel.ChannelOption;
import io.netty.handler.logging.LogLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.netty.transport.logging.AdvancedByteBufFormat;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class ExternalApiConfig {

    private final ExternalApiProperties externalApiProperties;
    private final WebClient.Builder defaultBuilder;

    @Bean
    public WebClient externalApiClient() {
        log.info("External API is at {}", externalApiProperties.url());
        return defaultBuilder
                .clientConnector(createHttpConnector())
                .baseUrl(externalApiProperties.url())
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.USER_AGENT, externalApiProperties.userAgent())
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + externalApiProperties.apiKey())
                .defaultStatusHandler(HttpStatusCode::isError, this::defaultErrorHandler)
                .build();
    }

    private ReactorClientHttpConnector createHttpConnector() {
        HttpClient httpClient = HttpClient.create()
                .wiretap("reactor.netty.http.client.HttpClient", LogLevel.DEBUG, AdvancedByteBufFormat.TEXTUAL)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, externalApiProperties.timeoutMillis());
        return new ReactorClientHttpConnector(httpClient);
    }

    private Mono<? extends Throwable> defaultErrorHandler(ClientResponse errorResponse) {
        ExternalApiException exception = new ExternalApiException(
            errorResponse.statusCode(), errorResponse.bodyToMono(String.class).block()
        );
        return Mono.error(exception);
    }

}
