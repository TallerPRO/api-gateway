package com.ms_tallerpro.gateway.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/** Propiedades propias del gateway (prefijo tallerpro.gateway en application.yml). */
@Getter
@Setter
@ConfigurationProperties(prefix = "tallerpro.gateway")
public class GatewayProperties {

    /** Si es false, el gateway no exige JWT (solo para desarrollo local sin tenant). */
    private boolean jwtEnabled = true;

    /** Origenes del frontend autorizados por CORS. */
    private List<String> allowedOrigins = List.of("http://localhost:4321");
}
