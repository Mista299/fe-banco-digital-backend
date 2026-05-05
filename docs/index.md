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

## Historias de usuario

| Documento | Descripción |
|-----------|-------------|
| [HU-01 Registro de usuarios](./hu/ft-01-hu-01-registro-nuevos-usuarios.md) | Registro de perfil, cliente y cuenta inicial |
| [HU-12 Transferencia a otros bancos](./hu/hu-12-transferencia-fondos-otros-bancos.md) | Transferencias interbancarias mediante proceso ACH |
| [HU-13 Motor de Validación](./hu/hu-13-motor-validacion.md) | Validación automática de saldo y estado de cuenta |
| [Task 59 y Task 60](./hu/task-59-60-transacciones.md) | Registro de transacciones y campo tipo_operacion |

## Casos de prueba

| Documento | Descripción |
|-----------|-------------|
| [Casos HU-12 / HU-13](./tests/iteration-2-hu-12-hu-13-test-cases.md) | Matriz de pruebas para Iteration 2 |
