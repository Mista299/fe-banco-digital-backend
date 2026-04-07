# ADR-001: JWT dual-token (access + refresh)

**Estado:** Aceptada  
**Fecha:** 2026-04-06

## Contexto

El sistema necesita autenticar usuarios en una API REST. La opción más simple es un JWT de larga duración (ej. 24h). Sin embargo, en un sistema bancario un token robado que dure horas es un riesgo inaceptable.

## Opciones consideradas

1. **JWT de larga duración (24h)** — simple, stateless, pero ventana de ataque amplia. Revocar un token comprometido requiere una blacklist.
2. **Sesiones en servidor** — fáciles de revocar, pero rompen el diseño stateless de REST y escalan mal.
3. **JWT de corta duración + Refresh Token** — access token stateless y efímero; refresh token revocable guardado en BD.

## Decisión

Se eligió la opción 3: **access token JWT de 10 minutos + refresh token UUID de 7 días persistido en BD**.

- El access token se valida en cada request sin tocar la BD (stateless).
- El refresh token permite renovar la sesión sin re-autenticación frecuente.
- Al hacer logout o detectar compromiso, se borra el refresh token de BD — revocación inmediata.
- Se aplica **Token Rotation**: cada uso del refresh token genera uno nuevo y borra el anterior.

## Consecuencias

- La tabla `refresh_token` crece con el tiempo — requiere limpieza periódica de tokens expirados.
- Dos requests en lugar de uno para renovar sesión, pero es infrecuente (cada 10 min).
- El servidor debe consultar la BD solo en `/refresh` y `/logout`, no en cada request normal.
