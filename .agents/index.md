# Índice — Contexto del proyecto para Claude

## Resumen del proyecto
- Sistema de banca digital (backend REST) construido con Java 17 + Spring Boot 3 + PostgreSQL.
- Gestiona clientes, cuentas bancarias, transacciones y auditoría.
- Seguridad con Spring Security + JWT dual-token (access 10 min / refresh 7 días).
- Arquitectura en capas estricta: Controller → Service → Repository. Saltar capas está prohibido.
- Todos los identificadores del código van en español (clases, métodos, variables, enums).
- Valores monetarios: siempre `BigDecimal` + `DECIMAL(19,4)` en BD. Nunca `double`/`float`.
- Endpoints nuevos bajo `/api/v1/...`. Siempre interfaz + implementación en servicios.
- Equipo: mista, mafe, bryan, xiomi, cristian. Ramas: `{usuario}/{descripcion}` desde `develop`.

## Archivos de contexto
- [context.md](./context.md) — módulos, relaciones clave, restricciones, antipatrones, comandos
- [design/architecture.md](./design/architecture.md) — capas, responsabilidades, reglas de comunicación
- [design/auth.md](./design/auth.md) — diseño JWT dual-token, flujos de registro/login/refresh/logout
- [design/database.md](./design/database.md) — modelo entidad-relación, tablas, enums JPA
- [design/methodology.md](./design/methodology.md) — reglas de código, Git Flow, estructura de carpetas
- [design/async.md](./design/async.md) — asincronismo: eventos de auditoría, pool de hilos, registro de fallos
- [sprint/current.md](./sprint/current.md) — sprint activo y HU priorizadas

## Convenciones
- [conventions/diagrams.md](./conventions/diagrams.md) — cómo generar diagramas con D2
- [conventions/documentation-guidelines.md](./conventions/documentation-guidelines.md) — reglas de documentación para humanos y para IA
- [conventions/docs.md](./conventions/docs.md) — guía para generar documentos Word del proyecto
