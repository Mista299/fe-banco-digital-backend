# Arquitectura del sistema

## Estilo arquitectónico

El sistema usa **arquitectura en capas** (Layered Architecture). Cada capa tiene una única responsabilidad y solo puede comunicarse con la capa inmediatamente adyacente. Saltarse una capa está prohibido.

![Arquitectura general](./diagrams/architecture.svg)

## Diagrama de paquetes y componentes

![Diagrama de paquetes y componentes](./diagrams/package-components.png)

## Capas

| Capa | Paquete | Responsabilidad |
|------|---------|-----------------|
| Presentación | `controller/` | Recibir HTTP, delegar al service, retornar `ResponseEntity` |
| Negocio | `service/` | Toda la lógica de negocio, validaciones, orquestación |
| Conversión | `mapper/` | Convertir entidades ↔ DTOs |
| Persistencia | `repository/` | Acceso a PostgreSQL vía JPA |
| Modelo | `entity/` | Representar tablas de la BD como clases Java |
| Transferencia | `dto/` | Objetos de entrada/salida de la API |
| Errores | `exception/` | Excepciones de negocio y `GlobalExceptionHandler` |
| Seguridad | `security/` | JWT, filtros y configuración Spring Security |
| Eventos | `event/` + `listener/` | Auditoría asíncrona tras cada operación exitosa |
| Configuración | `config/` | Pool de hilos para tareas asíncronas |

## Reglas de comunicación

- **Controller** → solo habla con Service (via interfaz). Nunca con Repository ni Mapper.
- **Service** → usa Repository y Mapper. Es el único lugar donde se inyecta el Mapper.
- **Repository** → solo devuelve entidades, nunca DTOs.
- **Mapper** → no depende de nadie. Solo convierte objetos.

## Estructura de carpetas

```
src/main/java/fe/banco_digital/
├── controller/
├── service/
│   ├── XxxService.java            ← interfaz
│   └── XxxServiceImpl.java        ← implementación
├── mapper/
├── repository/
├── entity/
├── dto/
├── exception/
│   └── GlobalExceptionHandler.java
├── security/
│   ├── JwtUtil.java
│   ├── FiltroJwt.java
│   ├── UsuarioDetallesService.java
│   └── ConfiguracionSeguridad.java
├── event/
│   └── AuditoriaEvent.java        ← evento publicado tras cada operación
├── listener/
│   └── AuditoriaEventListener.java ← persiste auditoría en hilo separado
└── config/
    └── ConfiguracionAsync.java    ← pool de hilos para tareas asíncronas
```

## Decisiones relacionadas

- [ADR-001 — JWT dual-token](./decisions/adr-001-jwt.md)
- [ADR-002 — Arquitectura en capas](./decisions/adr-002-layered-architecture.md)
