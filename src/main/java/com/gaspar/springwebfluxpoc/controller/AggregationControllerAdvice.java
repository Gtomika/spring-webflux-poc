package com.gaspar.springwebfluxpoc.controller;

import com.gaspar.springwebfluxpoc.exception.ExternalApiException;
import com.gaspar.springwebfluxpoc.model.ErrorResponse;
import io.netty.handler.timeout.TimeoutException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@Slf4j
@RestControllerAdvice
public class AggregationControllerAdvice {

    @ExceptionHandler(ExternalApiException.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public ErrorResponse handleExternalApiException(ExternalApiException e) {
        return new ErrorResponse("external_api_error", e.getMessage(), LocalDateTime.now());
    }

    @ExceptionHandler(TimeoutException.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public ErrorResponse handleTimeoutException(TimeoutException e) {
        return new ErrorResponse("external_api_timeout", e.getMessage(), LocalDateTime.now());
    }

}
