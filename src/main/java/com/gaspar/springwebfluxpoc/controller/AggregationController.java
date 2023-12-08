package com.gaspar.springwebfluxpoc.controller;

import com.gaspar.springwebfluxpoc.model.AggregatedResponse;
import com.gaspar.springwebfluxpoc.service.AggregationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/aggregation")
public class AggregationController {

    private final AggregationService aggregationService;

    @GetMapping("/{userId}")
    public AggregatedResponse aggregateUserData(@PathVariable UUID userId) {
        return aggregationService.aggregateUserData(userId);
    }

}
