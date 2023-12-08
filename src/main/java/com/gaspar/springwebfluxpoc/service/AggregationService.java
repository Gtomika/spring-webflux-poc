package com.gaspar.springwebfluxpoc.service;

import com.gaspar.springwebfluxpoc.config.properties.ExternalApiProperties;
import com.gaspar.springwebfluxpoc.model.AggregatedResponse;
import com.gaspar.springwebfluxpoc.model.PaymentResponse;
import com.gaspar.springwebfluxpoc.model.ReservationResponse;
import com.gaspar.springwebfluxpoc.model.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AggregationService {

    private final WebClient externalApiClient;
    private final ExternalApiProperties externalApiProperties;

    @Cacheable(cacheNames = "aggregated-user-data")
    public AggregatedResponse aggregateUserData(UUID userId) {
        var userMono = fetchUser(userId);
        var reservationsMono = fetchReservations(userId);
        var paymentsMono = fetchPayments(userId);

        return Mono.zip(userMono, reservationsMono, paymentsMono)
                .flatMap(tuple -> {
                    UserResponse user = tuple.getT1();
                    List<ReservationResponse> reservations = tuple.getT2();
                    List<PaymentResponse> payments = tuple.getT3();
                    AggregatedResponse aggregatedResponse = new AggregatedResponse(user, reservations, payments);
                    return Mono.just(aggregatedResponse);
                })
                .block();
    }

    private Mono<UserResponse> fetchUser(UUID userId) {
        return externalApiClient.get()
                .uri(externalApiProperties.userPath(), userId)
                .retrieve()
                .bodyToMono(UserResponse.class);
    }

    private Mono<List<ReservationResponse>> fetchReservations(UUID userId) {
        return externalApiClient.get()
                .uri(externalApiProperties.reservationsPath(), userId)
                .retrieve()
                .bodyToFlux(ReservationResponse.class)
                .collectList();
    }

    private Mono<List<PaymentResponse>> fetchPayments(UUID userId) {
        return externalApiClient.get()
                .uri(externalApiProperties.paymentHistoryPath(), userId)
                .retrieve()
                .bodyToFlux(PaymentResponse.class)
                .collectList();
    }

}
