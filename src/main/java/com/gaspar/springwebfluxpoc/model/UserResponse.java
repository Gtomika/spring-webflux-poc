package com.gaspar.springwebfluxpoc.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

public record UserResponse(
        UUID userId,
        String name,
        String email,
        LocalDate birthday
) implements Serializable {}
