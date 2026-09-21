package com.ms_tallerpro.gateway.config;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.ResourceAccessException;

/**
 * Contrato de errores (ARQUITECTURA_ACCESO.md, seccion 8): si el microservicio
 * destino no responde, el frontend debe recibir 503 y mostrar un banner de
 * "servicio no disponible", no un 500 generico ni un spinner infinito.
 */
@Slf4j
@RestControllerAdvice
public class UpstreamErrorHandler {

    @ExceptionHandler(ResourceAccessException.class)
    ProblemDetail upstreamUnavailable(ResourceAccessException ex, HttpServletRequest request) {
        log.warn("Microservicio no disponible para {} {}: {}", request.getMethod(), request.getRequestURI(), ex.getMessage());
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.SERVICE_UNAVAILABLE);
        problem.setTitle("Servicio no disponible");
        problem.setDetail("El servicio que atiende esta ruta no responde. Intenta nuevamente en unos instantes.");
        return problem;
    }
}
