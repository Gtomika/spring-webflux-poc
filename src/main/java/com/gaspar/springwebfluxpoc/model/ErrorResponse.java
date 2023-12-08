package com.gaspar.springwebfluxpoc.model;

import java.time.LocalDateTime;

public record ErrorResponse(
        String errorCode,
        String message,
        LocalDateTime timestamp
) {
}
