package com.ms_tallerpro.gateway.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Capa 2 del control de acceso (ARQUITECTURA_ACCESO.md, seccion 5): el gateway
 * comprueba que el token sea legitimo (firma contra el JWKS del tenant, iss,
 * aud, exp/nbf). Si algo falla responde 401 y la request nunca llega a un
 * microservicio. La AUTORIZACION por rol (403) NO se decide aqui: la decide cada
 * microservicio con hasRole / @PreAuthorize, porque el gateway puede ser evadido
 * y cada servicio debe validar por su cuenta (confianza cero).
 *
 * Las rutas publicas son solo las de salud, para Docker/EC2.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final String[] PUBLIC = {"/actuator/health", "/actuator/health/**", "/actuator/info"};

    /** Modo normal: todo /api/** exige Bearer JWT valido del tenant. */
    @Bean
    @ConditionalOnProperty(prefix = "tallerpro.gateway", name = "jwt-enabled", havingValue = "true", matchIfMissing = true)
    SecurityFilterChain jwtFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(PUBLIC).permitAll()
                        .anyRequest().authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())));
        return http.build();
    }

    /** Modo desarrollo local (TALLERPRO_JWT_ENABLED=false): sin tenant, solo CORS. */
    @Bean
    @ConditionalOnProperty(prefix = "tallerpro.gateway", name = "jwt-enabled", havingValue = "false")
    SecurityFilterChain openFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        return http.build();
    }

    /** App roles de Azure AD (claim "roles") -> authorities ROLE_Admin, ROLE_JefeTaller, etc. */
    private JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter authorities = new JwtGrantedAuthoritiesConverter();
        authorities.setAuthoritiesClaimName("roles");
        authorities.setAuthorityPrefix("ROLE_");

        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(authorities);
        return converter;
    }
}
