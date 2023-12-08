package com.gaspar.springwebfluxpoc.model;

import java.io.Serializable;
import java.util.List;

public record AggregatedResponse(
        UserResponse user,
        List<ReservationResponse> reservations,
        List<PaymentResponse> payments
) implements Serializable {}
