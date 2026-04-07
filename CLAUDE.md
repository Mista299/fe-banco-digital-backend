# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

> Punto de entrada: [`.agents/index.md`](.agents/index.md)
>
> Contexto rápido:
> - **Contexto general, módulos y restricciones** → [`.agents/context.md`](.agents/context.md)
> - **Arquitectura en capas** → [`.agents/design/architecture.md`](.agents/design/architecture.md)
> - **Autenticación JWT** → [`.agents/design/auth.md`](.agents/design/auth.md)
> - **Modelo de base de datos** → [`.agents/design/database.md`](.agents/design/database.md)
> - **Metodología y reglas de código** → [`.agents/design/methodology.md`](.agents/design/methodology.md)
> - **Sprint actual** → [`.agents/sprint/current.md`](.agents/sprint/current.md)

### Convenciones
- Diagramas: ver `.agents/conventions/diagrams.md`
- Documentación: ver `.agents/conventions/documentation-guidelines.md`
- Documentos Word: ver `.agents/conventions/docs.md`

---

## Commands

```bash
# Run the application (loads .env automatically)
./run.sh

# Run without the script (requires env vars exported)
./mvnw spring-boot:run

# Run with seed data (creates roles, clients, accounts, transactions)
./mvnw spring-boot:run -Dspring-boot.run.profiles=seed

# Run tests (uses H2 in-memory, no PostgreSQL needed)
./mvnw test

# Run a single test class
./mvnw test -Dtest=BancoDigitalApplicationTests

# Build without running tests
./mvnw package -DskipTests
```

---

## Database configuration

The app connects to PostgreSQL via environment variables. Copy `.env` values as needed:

| Variable   | Default                                      |
|------------|----------------------------------------------|
| `DB_URL`   | `jdbc:postgresql://localhost:5432/banco2026` |
| `DB_USER`  | `postgres`                                   |
| `DB_PASS`  | _(empty)_                                    |
| `DDL_AUTO` | `create` (drops and recreates schema on every start) |

In `.env` (gitignored), `DDL_AUTO=update` is set so that the local dev DB schema persists across restarts. The default in `application.properties` is `create`, which is intentional for fresh environments — change it to `update` once the schema is stable.

There is also `application-local.properties.example` showing the alternative approach of using a Spring profile instead of `.env`.

---

## Architecture overview

Spring Boot 4 / Java 17 layered application with no security framework wired yet.

**Layer flow**: `Controller → Service interface → ServiceImpl → Repository`

**Domain model** (`entity/`) — ver detalle completo en [`.agents/DATABASE.md`](.agents/DATABASE.md):
- `Cliente` — bank customer (nombre, documento, email, telefono). Has a `List<Cuenta>`.
- `Cuenta` — bank account linked to a `Cliente`. Has `TipoCuenta` (AHORROS, CORRIENTE), `EstadoCuenta` (ACTIVA, INACTIVA), and `saldo`.
- `Usuario` — login credential linked 1-to-1 to a `Cliente`. Has `EstadoUsuario` and a many-to-many with `Rol`.
- `Rol` / `RolNombre` (ADMIN, CLIENTE) — role-based access structure (not enforced yet, no Spring Security).
- `Transaccion` — references `cuentaOrigen` and `cuentaDestino` (both `Cuenta`). Has `TipoTransaccion` (DEPOSITO, RETIRO, TRANSFERENCIA).
- `Auditoria` — free-form audit log (accion, usuario string, detalle).

**Seed data** (`DataLoader`): activated only under the `seed` Spring profile. Idempotent — uses `findBy…OrElseGet` before inserting. Creates two clients (bryan/ana), three accounts, three transactions, and two audit entries.

**Currently implemented endpoints**:
- `GET /api/profile/{userId}` — returns `ProfileDTO` (fullName, identificationNumber, accountNumber, balance) by looking up `Cliente` + first active `Cuenta` for that client ID.
- `GET /api/db-ping` — simple DB connectivity check (`DbPingController`).

**Error handling**: `GlobalExceptionHandler` catches all `Exception` and returns 500 with a generic Spanish message.

**Tests**: only a context-load smoke test exists. Tests override the datasource to H2 (`MODE=PostgreSQL`) with `create-drop`, so no external DB is required.

---

## Implementation gap — código existente vs. modelo objetivo

`DATABASE.md` describe el esquema **objetivo**; el código actual es una implementación parcial. Al escribir código nuevo, seguir el modelo objetivo, no el estilo del código legacy:

| Aspecto | Código actual | Objetivo (DATABASE.md / METHODOLOGY.md) |
|---------|--------------|------------------------------------------|
| Campos de fecha | `Instant` | `LocalDateTime` |
| Entidades | Getters/setters manuales | Lombok (`@Getter`, `@Setter`, `@NoArgsConstructor`) |
| Estado de `Transaccion` | Sin campo `estado` | Enum `EstadoTransaccion` (EXITOSA, FALLIDA) |
| `Auditoria.usuario` | `String` (username) | FK → `Usuario` |
| Fetch type | Por defecto (EAGER) | `FetchType.LAZY` en todas las relaciones |
| URL base | `/api/...` | `/api/v1/...` para todos los endpoints nuevos |

---

## Git Flow (resumen)

> Detalle completo en [`.agents/METHODOLOGY.md`](.agents/METHODOLOGY.md) — sección 9.

- Ramas base: `main` (producción) → `develop` (integración)
- Cada feature se trabaja en una rama personal: `{usuario}/{descripcion}`
- Integrantes: `mista`, `mafe`, `bryan`, `xiomi`, `cristian`
- Ejemplos válidos: `mista/feat-login`, `bryan/fix-calculo-saldo`
- Siempre partir desde `develop` actualizado antes de crear una rama
- Sincronizar con `develop` cada 1-2 días para evitar conflictos
- Al terminar: PR hacia `develop`, mínimo 1 aprobación
- Nunca hacer merge directo a `main`

---

## Coding rules (resumen)

> Reglas completas en [`.agents/METHODOLOGY.md`](.agents/METHODOLOGY.md)

- Todo el código (clases, variables, métodos, enums) en **español**
- Nunca lógica de negocio en controllers ni repositories
- Siempre crear interfaz del servicio + su implementación separada
- Nunca exponer entidades JPA directamente — usar DTOs
- Usar `BigDecimal` para valores monetarios, nunca `double` o `float`
- Usar `LocalDateTime` para fechas
- Todo manejo de errores centralizado en `GlobalExceptionHandler`
- Las operaciones de escritura llevan `@Transactional`
- Todos los endpoints nuevos bajo `/api/v1/...`
