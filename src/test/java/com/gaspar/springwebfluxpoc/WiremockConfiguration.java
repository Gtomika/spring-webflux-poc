package com.gaspar.springwebfluxpoc;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.contract.wiremock.WireMockConfigurationCustomizer;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class WiremockConfiguration {

    @Bean
    WireMockConfigurationCustomizer optionsCustomizer() {
        return options -> {
            //optionally, we could customize WireMock here
        };
    }

}
