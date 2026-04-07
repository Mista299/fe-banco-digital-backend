# Banco Digital — Documentación

Sistema de banca digital (backend REST) construido con Java 17 + Spring Boot 3 + PostgreSQL.

## Guías

| Documento | Descripción |
|-----------|-------------|
| [Cómo ejecutar el proyecto](./guides/getting-started.md) | Requisitos, variables de entorno, comandos |
| [Datos de prueba (seed)](./guides/seed.md) | Cómo cargar datos iniciales y qué contienen |
| [Consumir la API](./guides/api-usage.md) | Autenticación y ejemplos de endpoints |
| [Flujo de trabajo en Git](./guides/git-workflow.md) | Ramas por HU, PRs, sincronización y merge al sprint |

## Arquitectura

| Documento | Descripción |
|-----------|-------------|
| [Visión general](./architecture.md) | Estilo arquitectónico, capas y reglas de comunicación |

## Módulos

| Documento | Descripción |
|-----------|-------------|
| [Autenticación](./modules/auth.md) | Registro, login, JWT dual-token, refresh y logout |
| [Clientes](./modules/clients.md) | Gestión del perfil del cliente |
| [Cuentas](./modules/accounts.md) | Cuentas bancarias, estados y tipos |
| [Transacciones](./modules/transactions.md) | Depósitos, retiros y transferencias |

## Decisiones técnicas

| Documento | Descripción |
|-----------|-------------|
| [ADR-001 — JWT dual-token](./decisions/adr-001-jwt.md) | Por qué access + refresh token en lugar de un solo JWT |
| [ADR-002 — Arquitectura en capas](./decisions/adr-002-layered-architecture.md) | Por qué se eligió arquitectura en capas |

## Diagramas

| Archivo | Descripción |
|---------|-------------|
| [architecture.svg](./diagrams/architecture.svg) | Capas del sistema y flujo general |
| [auth-flow.svg](./diagrams/auth-flow.svg) | Flujo de autenticación JWT |
| [database.svg](./diagrams/database.svg) | Modelo entidad-relación |
