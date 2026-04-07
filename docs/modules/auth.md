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

## Tokens

| Token | Duración | Almacenamiento | Para qué |
|-------|----------|----------------|----------|
| Access Token (JWT) | 10 minutos | Solo en el cliente | Acceder a rutas protegidas |
| Refresh Token (UUID) | 7 días | BD + cliente | Renovar el access token |

El access token es **stateless** — el servidor no lo guarda. El refresh token es **stateful** — se persiste en la tabla `refresh_token` y se puede revocar.

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

Respuesta:
```json
{
  "accessToken": "eyJ...",
  "refreshToken": "550e8400-e29b-41d4-a716-...",
  "tipo": "Bearer",
  "expiraEn": 600000
}
```

**Usar el access token en rutas protegidas:**
```http
GET /api/v1/profile/1
Authorization: Bearer eyJ...
```

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
