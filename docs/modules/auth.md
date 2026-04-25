# Módulo de Autenticación

## ¿Qué hace?

Gestiona el ciclo completo de autenticación: registro de usuarios, login con JWT, renovación de tokens y cierre de sesión.

## Responsabilidades

- Registrar un nuevo usuario vinculándolo a un cliente existente
- Autenticar credenciales y emitir un par de tokens (access + refresh)
- Renovar el access token usando el refresh token (con rotación automática)
- Revocar el refresh token al cerrar sesión

## Lo que NO hace

- No crea el cliente (`Cliente`) — ese ya debe existir en BD
- No gestiona permisos por recurso (eso lo hace `ConfiguracionSeguridad`)
- No envía emails ni notificaciones de seguridad

## Diagrama

![Flujo de autenticación](../diagrams/auth-flow.svg)

## Endpoints

| Método | Ruta | Descripción | Auth requerida |
|--------|------|-------------|----------------|
| `POST` | `/api/v1/auth/registro` | Registrar nuevo usuario | No |
| `POST` | `/api/v1/auth/login` | Login con username/password | No |
| `POST` | `/api/v1/auth/refresh` | Renovar access token | No (usa refresh token) |
| `POST` | `/api/v1/auth/logout` | Cerrar sesión | No (usa refresh token) |

## Tokens y cookies

| Token | Duración | Almacenamiento | Para qué |
|-------|----------|----------------|----------|
| Access Token (JWT) | 10 minutos | Cookie `HttpOnly` | Acceder a rutas protegidas |
| Refresh Token (UUID) | 7 días | BD + cookie `HttpOnly` | Renovar el access token |

El access token es **stateless** — el servidor no lo guarda. El refresh token es **stateful** — se persiste en la tabla `refresh_token` y se puede revocar.

Los tokens viajan únicamente en cookies `HttpOnly` (JavaScript no puede leerlas). En producción con HTTPS, las cookies también tienen `.secure()` y `SameSite=Strict` activados mediante la variable de entorno `HTTPS_SEGURO=true`.

## CORS

Los orígenes permitidos se configuran en la variable de entorno `CORS_ORIGENES` (separados por coma). Por defecto: `http://localhost:3000,http://localhost:5173`.

## Cómo se usa

**Registro:**
```http
POST /api/v1/auth/registro
Content-Type: application/json

{
  "username": "bryan123",
  "password": "segura123",
  "idCliente": 1
}
```

**Login:**
```http
POST /api/v1/auth/login
Content-Type: application/json

{
  "username": "bryan123",
  "password": "segura123"
}
```

Respuesta: `200 OK` con cookies `HttpOnly` seteadas. El cuerpo solo contiene:
```json
{ "mensaje": "Sesión iniciada exitosamente" }
```

**Las rutas protegidas se acceden automáticamente** con las cookies de sesión (el navegador las envía solo). No se necesita `Authorization: Bearer` en el header.

## Dependencias

| Módulo / Clase | Para qué |
|----------------|----------|
| `UsuarioRepository` | Verificar existencia, guardar usuario |
| `ClienteRepository` | Buscar el cliente al que se vincula el usuario |
| `RefreshTokenRepository` | Persistir y revocar refresh tokens |
| `JwtUtil` | Generar y validar access tokens |
| `BCryptPasswordEncoder` | Hash de contraseñas |

## Decisiones relacionadas

- [ADR-001 — JWT dual-token](../decisions/adr-001-jwt.md)
