# Convenciones de Documentación para Humanos

## Principio general

La documentación para humanos vive en `docs/`. Claude la **genera y actualiza**,
pero está escrita para ser leída por personas, no por agentes.

No mezclar instrucciones operativas para Claude dentro de `docs/`.
Esas van en `.claude/`.

---

## Estructura de docs/

```
docs/
├── index.md                  ← índice general, punto de entrada
├── architecture.md           ← visión general del sistema
├── modules/
│   ├── auth.md
│   ├── payments.md
│   └── ...
├── decisions/
│   ├── adr-001-nombre.md     ← Architecture Decision Records
│   └── ...
└── diagrams/
    ├── architecture.d2       ← fuente editable
    ├── architecture.svg      ← generado, embebido en los .md
    └── ...
```

---

## Plantilla para documentar un módulo

Usar esta estructura al documentar cualquier módulo o servicio:

```markdown
# Nombre del módulo

## ¿Qué hace?
Una o dos frases. Sin jerga técnica innecesaria.

## Responsabilidades
- Responsabilidad 1
- Responsabilidad 2

## Lo que NO hace
(igual de importante — evita que otros pongan cosas aquí que no corresponden)

## Diagrama
![Nombre](../diagrams/nombre-modulo.svg)

## Cómo se usa
Ejemplo concreto con código real del proyecto, no inventado.

## Dependencias
| Módulo | Para qué |
|--------|----------|
| Auth   | Verificar permisos |

## Decisiones relevantes
Links a ADRs relacionados.
```

---

## Architecture Decision Records (ADR)

Crear un ADR cada vez que se tome una decisión técnica importante o no obvia.

### Cuándo crear un ADR
- Se elige una librería sobre otra
- Se define un patrón que todos deben seguir
- Se descarta una alternativa que parece obvia
- Se cambia una decisión anterior

### Plantilla ADR

```markdown
# ADR-XXX: Título corto

**Estado:** Propuesta | Aceptada | Deprecada | Reemplazada por ADR-YYY
**Fecha:** YYYY-MM-DD

## Contexto
¿Qué situación o problema originó esta decisión?

## Opciones consideradas
1. Opción A — pro / contra
2. Opción B — pro / contra

## Decisión
Qué se eligió y por qué.

## Consecuencias
Qué implica esta decisión a futuro.
```

Nombrar los archivos: `adr-001-descripcion-corta.md`

---

## Tono y estilo

- Escribir en el idioma del equipo (definir uno y no mezclar)
- Párrafos cortos, máximo 4 líneas
- Preferir listas sobre párrafos densos
- Usar ejemplos reales del proyecto, no abstractos
- Evitar frases como "simplemente", "obviamente", "es fácil"

---

## Lo que Claude NO debe documentar

- Código autoexplicativo (nombres de variables, funciones simples)
- Implementaciones que pueden cambiar frecuentemente sin aviso
- TODOs o deuda técnica → esos van en issues del repositorio, no en `.md`

---

## Mantenimiento

- Al modificar un módulo, actualizar su `.md` en el mismo PR/commit
- Si una decisión cambia, no borrar el ADR — marcarlo como `Deprecada` y crear uno nuevo
- El `docs/index.md` debe reflejar siempre la estructura real de `docs/`
