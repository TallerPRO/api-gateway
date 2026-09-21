# ms-tallerpro-gateway

API Gateway de **TallerPro**. Único punto de entrada del frontend: valida el JWT de Azure AD
y enruta cada request al microservicio de dominio que corresponde.

Spring Boot 4.1 · Java 25 · Spring Cloud Gateway (WebMVC) · Spring Security OAuth2 Resource Server.

```
SPA (Astro/React) --Bearer JWT--> Gateway :8080 --> ms-tallerpro-jobs    :8081
                                                --> ms-tallerpro-catalog :8082
                                                --> ms-tallerpro-report  :8085
                                                --> ms-tallerpro-audit   :8086
```

## Rutas

| Prefijo en el gateway | Microservicio | Variable de destino |
|---|---|---|
| `/api/v1/ordenes/**` | ms-tallerpro-jobs | `JOBS_URL` (default `http://localhost:8081`) |
| `/api/v1/talleres/**`, `/api/v1/servicios/**` | ms-tallerpro-catalog | `CATALOG_URL` (default `http://localhost:8082`) |
| `/api/report/**` | ms-tallerpro-report | `REPORT_URL` (default `http://localhost:8085`) |
| `/api/audit/**` | ms-tallerpro-audit | `AUDIT_URL` (default `http://localhost:8086`) |
| `/actuator/health` | (propio, público) | — |

El path se reenvía tal cual (sin reescritura) y la cabecera `Authorization` se propaga al
microservicio, que vuelve a validar el token por su cuenta (confianza cero).

## Qué valida el gateway

Con `TALLERPRO_JWT_ENABLED=true` (default), toda ruta `/api/**` exige `Authorization: Bearer <jwt>` y
se comprueba: firma contra el JWKS del tenant, `iss`, `aud`, `exp`/`nbf`. Si falla → **401**.
La autorización por rol (403) la decide cada microservicio.

| Situación | Respuesta |
|---|---|
| Sin token / token inválido o vencido | `401` |
| Origen no permitido por CORS | bloqueado por el navegador |
| Microservicio destino caído | `503` con `application/problem+json` |
| Ruta no mapeada | `404` |

## Variables de entorno

| Variable | Default | Descripción |
|---|---|---|
| `SERVER_PORT` | `8080` | Puerto del gateway |
| `TALLERPRO_JWT_ENABLED` | `true` | `false` solo para desarrollo local sin tenant |
| `AZURE_TENANT_ID` | — | Tenant de Azure AD (issuer `https://login.microsoftonline.com/<tenant>/v2.0`) |
| `AZURE_API_CLIENT_ID` | — | Client ID de la API (audience `api://<client-id>`) |
| `FRONTEND_ORIGINS` | `http://localhost:4321` | Orígenes permitidos por CORS, separados por coma |
| `JOBS_URL`, `CATALOG_URL`, `REPORT_URL`, `AUDIT_URL` | `localhost:808x` | URL base de cada microservicio |

> El App Registration debe tener `accessTokenAcceptedVersion = 2`; de lo contrario el `iss` del token
> será `https://sts.windows.net/<tenant>/` y todas las llamadas darán `401`.

## Ejecución

```bash
# Desarrollo local sin tenant
TALLERPRO_JWT_ENABLED=false ./mvnw spring-boot:run

# Con validación JWT
AZURE_TENANT_ID=... AZURE_API_CLIENT_ID=... ./mvnw spring-boot:run

# Docker
docker build -t tallerpro/gateway .
docker run -p 8080:8080 -e TALLERPRO_JWT_ENABLED=false tallerpro/gateway
```

## Pruebas

```bash
./mvnw test
```

Cubre: sin token → 401, token inválido → 401, `/actuator/health` público, preflight CORS del frontend.

Prueba manual (sección 9 de `ARQUITECTURA_ACCESO.md`):

```bash
curl -i http://localhost:8080/api/v1/ordenes                      # 401 sin token
curl -i -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/v1/ordenes   # 200 con token válido
```
