package com.cts.configserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;

/**
 * Centralized configuration server for the Travel360 platform.
 *
 * <p>Every microservice boots as a Config Client and pulls its properties from
 * here (ports, datasource, JWT secret, Eureka, Feign settings) so configuration
 * is externalized in one place instead of being duplicated per service.</p>
 */
@SpringBootApplication
@EnableConfigServer
public class ConfigServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(ConfigServerApplication.class, args);
	}
}
