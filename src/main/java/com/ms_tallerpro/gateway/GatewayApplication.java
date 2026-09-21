package com.ms_tallerpro.gateway;

import com.ms_tallerpro.gateway.config.GatewayProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * ms-tallerpro-gateway: unico punto de entrada del frontend.
 * Flujo: SPA -> [JWT] -> Gateway -> microservicio de dominio.
 */
@SpringBootApplication
@EnableConfigurationProperties(GatewayProperties.class)
public class GatewayApplication {

	public static void main(String[] args) {
		SpringApplication.run(GatewayApplication.class, args);
	}

}
