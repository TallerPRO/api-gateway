package com.ms_tallerpro.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Evidencia de la capa de validacion (ARQUITECTURA_ACCESO.md, seccion 9, prueba 1):
 * sin token el gateway responde 401 antes de tocar cualquier microservicio.
 */
@SpringBootTest
@AutoConfigureMockMvc
class GatewayApplicationTests {

	@Autowired
	private MockMvc mockMvc;

	/** Sin tenant real en los tests: el decoder rechaza cualquier token, como haria con firma/issuer invalidos. */
	@MockitoBean
	private JwtDecoder jwtDecoder;

	@Test
	void sinTokenRespondeUnauthorized() throws Exception {
		mockMvc.perform(get("/api/v1/ordenes"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void tokenInvalidoRespondeUnauthorized() throws Exception {
		when(jwtDecoder.decode(anyString())).thenThrow(new BadJwtException("firma o issuer invalidos"));
		mockMvc.perform(get("/api/audit/timeline").header("Authorization", "Bearer no-es-un-jwt"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void healthEsPublico() throws Exception {
		mockMvc.perform(get("/actuator/health"))
				.andExpect(status().isOk());
	}

	@Test
	void preflightCorsDelFrontendPermitido() throws Exception {
		mockMvc.perform(options("/api/v1/ordenes")
						.header("Origin", "http://localhost:4321")
						.header("Access-Control-Request-Method", "GET")
						.header("Access-Control-Request-Headers", "authorization"))
				.andExpect(status().isOk())
				.andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:4321"));
	}

}
