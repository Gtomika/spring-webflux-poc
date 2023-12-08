package com.gaspar.springwebfluxpoc.model;

import java.io.Serializable;
import java.time.LocalDateTime;

public record PaymentResponse(
        int amount,
        String currency,
        PaymentType paymentType,
        boolean successfulPayment,
        LocalDateTime timestamp
) implements Serializable {}
