# ADR-002: Arquitectura en capas

**Estado:** Aceptada  
**Fecha:** 2026-04-06

## Contexto

El equipo es de cinco desarrolladores, varios en etapa de aprendizaje. Se necesita un estilo arquitectónico que permita trabajar en paralelo sin conflictos frecuentes y que sea fácil de entender y aplicar consistentemente.

## Opciones consideradas

1. **Arquitectura en capas (Layered)** — estándar en Spring Boot, baja curva de aprendizaje, estructura predecible.
2. **Arquitectura hexagonal (Ports & Adapters)** — más flexible y testeable, pero requiere mayor madurez del equipo y agrega complejidad conceptual.
3. **Arquitectura orientada a microservicios** — descartada por ser excesiva para el alcance del proyecto.

## Decisión

Se eligió **arquitectura en capas** con comunicación estricta en un solo sentido (Controller → Service → Repository).

La regla de no saltarse capas se refuerza con:
- Interfaz obligatoria en cada servicio (Controller depende de la interfaz, no de la implementación).
- Mapper como capa explícita separada (no incrustado en controllers ni repositories).
- `GlobalExceptionHandler` como único punto de manejo de errores.

## Consecuencias

- El código es predecible: dado un endpoint, el flujo siempre sigue la misma ruta.
- Añadir funcionalidad nueva sigue siempre el mismo patrón (Controller + Service + Mapper + Repository).
- La arquitectura en capas puede generar acoplamiento vertical si se ignoran las reglas — por eso están explícitamente documentadas.
