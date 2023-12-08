package com.gaspar.springwebfluxpoc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class SpringWebfluxPocApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringWebfluxPocApplication.class, args);
	}

}
