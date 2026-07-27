package com.cts.apigateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Single entry point for all external traffic into the Travel360 platform.
 *
 * <p>Routing rules are externalized to the Config Server ({@code api-gateway.yml})
 * and resolved against Eureka, so every client hits the gateway on port 8080 and
 * is forwarded to the owning microservice by path predicate.</p>
 */
@SpringBootApplication
public class ApiGatewayApplication {

	public static void main(String[] args) {
		SpringApplication.run(ApiGatewayApplication.class, args);
	}
}
