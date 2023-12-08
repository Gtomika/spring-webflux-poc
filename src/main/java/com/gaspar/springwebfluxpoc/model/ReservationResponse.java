package com.gaspar.springwebfluxpoc.model;

import java.io.Serializable;
import java.time.LocalDate;

public record ReservationResponse(
        String hotelName,
        LocalDate startDay,
        LocalDate endDay
) implements Serializable {}
