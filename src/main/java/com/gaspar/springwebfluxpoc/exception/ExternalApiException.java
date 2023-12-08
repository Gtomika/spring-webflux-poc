package com.gaspar.springwebfluxpoc.exception;

import org.springframework.http.HttpStatusCode;

public class ExternalApiException extends RuntimeException {

    public ExternalApiException(HttpStatusCode code, String body) {
        super("External API error occurred. Status code: %d. Body:\n%s".formatted(code.value(), body));
    }

}
