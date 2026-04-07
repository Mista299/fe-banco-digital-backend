# Autenticación — JWT + Refresh Token
> **Banco Digital** · Implementado en rama `autentication/mista`

---

## Estrategia de tokens

```
┌──────────────────────┬──────────────────────────────────┐
│   ACCESS TOKEN       │   REFRESH TOKEN                  │
├──────────────────────┼──────────────────────────────────┤
│  Duración: 10 min    │  Duración: 7 días                │
│  Tipo: JWT firmado   │  Tipo: UUID opaco                │
│  Vive: solo cliente  │  Vive: BD + cliente              │
│  Stateless           │  Stateful (guardado en BD)       │
│  Para: acceder APIs  │  Para: renovar el access token   │
└──────────────────────┴──────────────────────────────────┘
```

| Escenario | Solo Access Token largo | Dual Token |
|---|---|---|
| Token robado | Atacante tiene acceso por horas | Ventana máxima de 10 minutos |
| Re-login del usuario | Obligatorio al expirar | No necesario (usa refresh) |
| Revocar acceso | Imposible sin blacklist | Borrar refresh token de BD |

---

## Estructura de archivos

```
src/main/java/fe/banco_digital/
│
├── security/
│   ├── JwtUtil.java                   → Genera y valida access tokens JWT
│   ├── FiltroJwt.java                 → Intercepta cada request y valida el token
│   ├── UsuarioDetallesService.java    → Adapta Usuario → UserDetails de Spring
│   └── ConfiguracionSeguridad.java    → Rutas públicas/protegidas y cadena de filtros
│
├── controller/
│   └── AutenticacionController.java   → /registro, /login, /refresh, /logout
│
├── service/
│   ├── AutenticacionService.java      → Interfaz
│   ├── AutenticacionServiceImpl.java  → Login y registro
│   ├── RefreshTokenService.java       → Interfaz
│   └── RefreshTokenServiceImpl.java   → Crear, rotar y revocar refresh tokens
│
├── mapper/
│   ├── UsuarioMapper.java             → RegistroRequestDTO ↔ Usuario, Usuario → UsuarioRegistradoDTO
│   └── AutenticacionMapper.java       → Arma el LoginResponseDTO
│
├── repository/
│   └── RefreshTokenRepository.java    → Acceso a la tabla refresh_token
│
├── entity/
│   └── RefreshToken.java              → Nueva entidad JPA
│
├── dto/
│   ├── LoginRequestDTO.java           → { username, password }
│   ├── LoginResponseDTO.java          → { accessToken, refreshToken, tipo, expiraEn }
│   ├── RegistroRequestDTO.java        → { username, password, idCliente }
│   ├── UsuarioRegistradoDTO.java      → { idUsuario, username, estado }
│   └── RefreshTokenRequestDTO.java    → { refreshToken }
│
└── exception/
    ├── CredencialesInvalidasException.java
    ├── UsuarioYaExisteException.java
    ├── TokenInvalidoException.java
    └── TokenExpiradoException.java
```

**Archivos modificados:**
- `pom.xml` → Spring Security + JJWT 0.12.6
- `GlobalExceptionHandler.java` → 4 nuevos handlers de auth
- `application.properties` → `jwt.secreto`, `jwt.expiracion-access-ms`, `jwt.expiracion-refresh-dias`
- `UsuarioRepository.java` → `existsByUsername()`

---

## Flujo 1 — Registro

```mermaid
sequenceDiagram
    actor U as Usuario
    participant C as AutenticacionController
    participant S as AutenticacionServiceImpl
    participant UM as UsuarioMapper
    participant BR as BCryptPasswordEncoder
    participant R as UsuarioRepository

    U->>C: POST /api/v1/auth/registro
    C->>S: registrar(dto)
    S->>R: existsByUsername() → false
    S->>R: findById(idCliente) → Cliente
    S->>UM: aEntidad(dto) → Usuario parcial
    S->>BR: encode(password) → hash
    Note over S: setPasswordHash(hash) + setCliente(cliente)
    S->>R: save(usuario)
    S->>UM: aDTO(usuario) → UsuarioRegistradoDTO
    C-->>U: 201 Created · { idUsuario, username, estado }
```

---

## Flujo 2 — Login

```mermaid
sequenceDiagram
    actor U as Usuario
    participant C as AutenticacionController
    participant S as AutenticacionServiceImpl
    participant AM as AuthenticationManager
    participant JU as JwtUtil
    participant RTS as RefreshTokenServiceImpl
    participant AUM as AutenticacionMapper

    U->>C: POST /api/v1/auth/login
    C->>S: login(dto)
    S->>AM: authenticate(username, password)
    Note over AM: Carga usuario de BD,<br/>compara BCrypt,<br/>verifica estado
    AM-->>S: Authentication ✓
    S->>JU: generarToken(username) → accessToken (10 min)
    S->>RTS: crearRefreshToken(idUsuario) → RefreshToken (7 días, guardado en BD)
    S->>AUM: aLoginResponseDTO(accessToken, refreshToken)
    C-->>U: 200 OK · { accessToken, refreshToken, tipo, expiraEn }
```

---

## Flujo 3 — Request protegida

```mermaid
sequenceDiagram
    actor U as Usuario
    participant F as FiltroJwt
    participant JU as JwtUtil
    participant SC as SecurityContext
    participant CTRL as CualquierController

    U->>F: GET /api/v1/... · Authorization: Bearer eyJ...
    F->>JU: extraerUsername(token) → username
    F->>JU: esValido(token, username)

    alt Token válido
        JU-->>F: true ✓
        F->>SC: setAuthentication(usuario)
        F->>CTRL: request pasa autenticada
        CTRL-->>U: 200 OK + datos
    else Token inválido o expirado
        JU-->>F: false / lanza excepción
        F->>CTRL: request sin autenticar
        CTRL-->>U: 401 Unauthorized
    end
```

---

## Flujo 4 — Refresh Token

```mermaid
sequenceDiagram
    actor U as Usuario
    participant C as AutenticacionController
    participant RTS as RefreshTokenServiceImpl
    participant JU as JwtUtil
    participant AUM as AutenticacionMapper

    Note over U,C: El access token expiró (pasaron 10 min)
    U->>C: POST /api/v1/auth/refresh · { refreshToken }
    C->>RTS: renovarToken(refreshToken)
    RTS->>RTS: ¿Existe en BD y no está revocado?

    alt Inválido o revocado
        RTS-->>C: TokenInvalidoException
        C-->>U: 401 · "El token no es válido o fue revocado"
    else Expirado (>7 días)
        RTS-->>C: TokenExpiradoException
        C-->>U: 401 · "La sesión expiró, inicie sesión nuevamente"
    else Vigente — Token Rotation
        Note over RTS: Borra el refresh token viejo,<br/>crea uno nuevo en BD
        RTS->>JU: generarToken(username) → nuevo accessToken
        RTS->>AUM: aLoginResponseDTO(...)
        C-->>U: 200 OK · { nuevo accessToken, nuevo refreshToken }
    end
```

---

## Flujo 5 — Logout

```mermaid
sequenceDiagram
    actor U as Usuario
    participant C as AutenticacionController
    participant RTS as RefreshTokenServiceImpl
    participant DB as PostgreSQL

    U->>C: POST /api/v1/auth/logout · { refreshToken }
    C->>RTS: revocarToken(refreshToken)
    RTS->>DB: DELETE FROM refresh_token WHERE token = ?
    C-->>U: 200 OK · { "mensaje": "Sesión cerrada exitosamente" }
```
