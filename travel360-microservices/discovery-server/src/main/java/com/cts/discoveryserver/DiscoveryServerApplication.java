package com.cts.discoveryserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * Netflix Eureka service registry for the Travel360 platform.
 *
 * <p>All services (and the gateway) register here on startup; Feign clients and
 * the gateway resolve {@code lb://SERVICE-NAME} URIs against this registry, so
 * services talk to each other by logical name rather than hard-coded host/port.</p>
 */
@SpringBootApplication
@EnableEurekaServer
public class DiscoveryServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(DiscoveryServerApplication.class, args);
	}
}
