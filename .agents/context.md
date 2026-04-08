# Contexto del proyecto

## Stack
- Lenguaje: Java 17
- Framework: Spring Boot 3
- BD: PostgreSQL 14+ (variable `DB_URL`, usuario `DB_USER`, clave `DB_PASS`)
- ORM: Spring Data JPA / Hibernate — `DDL_AUTO=update` en local, `create` por defecto
- Seguridad: Spring Security + JJWT 0.12.6
- Lombok, Spring Validation, SpringDoc OpenAPI (Swagger)
- Tests: H2 en memoria (`MODE=PostgreSQL`, `create-drop`) — no requiere PostgreSQL externo

## Módulos principales
- `controller/` → recibe HTTP, delega al service, retorna `ResponseEntity`. Sin lógica de negocio.
- `service/` → interfaz + impl. Toda la lógica de negocio, validaciones, orquestación.
- `mapper/` → único punto de conversión entidad ↔ DTO. Un mapper por entidad, `@Component`.
- `repository/` → extiende `JpaRepository`. Solo devuelve entidades, nunca DTOs.
- `entity/` → clases `@Entity` con Lombok. Nunca salen del repository sin pasar por el mapper.
- `dto/` → objetos de entrada/salida. Protegen las entidades de quedar expuestas.
- `exception/` → excepciones de negocio + `GlobalExceptionHandler` (`@RestControllerAdvice`).
- `security/` → `JwtUtil`, `FiltroJwt`, `UsuarioDetallesService`, `ConfiguracionSeguridad`.

## Relaciones clave entre módulos
- Controller solo habla con Service (via interfaz). Nunca con Repository ni Mapper directamente.
- Service inyecta Repository y Mapper. Es el único lugar donde se usa el Mapper.
- Mapper no depende de nadie — solo convierte objetos.
- `GlobalExceptionHandler` captura excepciones lanzadas desde Service y devuelve HTTP coherente.
- `FiltroJwt` intercepta cada request antes de llegar al controller y valida el JWT.
- `RefreshToken` es una entidad JPA persistida en BD — el access token es stateless, el refresh es stateful.

## Restricciones de negocio
- `BigDecimal` obligatorio para todo valor monetario (`DECIMAL(19,4)` en BD).
- `LocalDateTime` para fechas (no `Instant`, no `Date`).
- `FetchType.LAZY` en todas las relaciones `@ManyToOne` y `@OneToMany`.
- Soft delete: cambiar campo `estado`, nunca borrar físicamente.
- Toda acción sensible (login, transferencia, cambio de estado) genera registro en `auditoria`.
- Contraseñas almacenadas con BCrypt. Nunca texto plano.
- Timestamps en UTC a nivel de BD.

## Antipatrones conocidos
- ❌ Lógica de negocio en controllers o repositories.
- ❌ Exponer entidades JPA directamente (sin pasar por Mapper y DTO).
- ❌ Inyectar Repository directamente en un Controller.
- ❌ Usar `double` o `float` para montos.
- ❌ Usar `EAGER` fetch — todo `LAZY`.
- ❌ Endpoints nuevos sin prefijo `/api/v1/`.
- ❌ Servicios sin interfaz separada.
- ❌ Try-catch en controllers o services salvo casos muy específicos.

## HU del Sprint 1 (activo)
- HU-01 Registro de nuevos usuarios → cristian
- HU-03 Consulta de perfil de clientes → bryan
- HU-04 Actualización de información del cliente → mista
- HU-05 Cierre del producto financiero → mafe
- HU-14 Exposición de movimientos y saldos históricos → xiomi
- HU-08 Activación de bloqueo preventivo → bryan

## Comandos frecuentes
```bash
./scripts/run.sh                                           # carga .env y ejecuta
./mvnw spring-boot:run                                     # sin .env
./mvnw spring-boot:run -Dspring-boot.run.profiles=seed    # con datos de prueba
./mvnw test                                                # tests con H2
./mvnw package -DskipTests                                 # build sin tests
```
