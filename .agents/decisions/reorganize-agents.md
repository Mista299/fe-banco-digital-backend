# Tarea: Reorganizar `.agents/` y generar archivos base

## Contexto
La carpeta `.agents/` existe pero no sigue la estructura correcta.
Los archivos actuales ya están escritos para Claude (listas cortas, no narrativa humana).
Hay que reorganizarlos sin perder contenido.

---

## Paso 1 — Reorganizar archivos existentes

Mover los archivos actuales a su nueva ubicación:

```
.agents/ARCHITECTURE.md     →  .agents/design/architecture.md
.agents/AUTH_DESIGN.md      →  .agents/design/auth.md
.agents/DATABASE.md         →  .agents/design/database.md
.agents/METHODOLOGY.md      →  .agents/design/methodology.md
.agents/CURRENT_SPRINT.md   →  .agents/sprint/current.md
.agents/PROJECT_CONTEXT.md  →  (contenido se absorbe en context.md — ver Paso 3)
.agents/DOCUMENTATION.md    →  (revisar si tiene contenido único, si no, eliminar)
```

Mover los CSV fuera de `.agents/`:
```
.agents/hu-s1.csv  →  planning/hu-s1.csv
.agents/hu-s2.csv  →  planning/hu-s2.csv
.agents/hu-s3.csv  →  planning/hu-s3.csv
```

---

## Paso 2 — Estructura final esperada

```
.agents/
├── index.md                         ← generar (Paso 3)
├── context.md                       ← generar (Paso 4)
├── conventions/
│   ├── diagrams.md
│   ├── docs.md
│   └── documentation-guidelines.md
├── design/
│   ├── architecture.md
│   ├── auth.md
│   ├── database.md
│   └── methodology.md
├── decisions/
│   └── (vacía por ahora)
└── sprint/
    └── current.md
```

---

## Paso 3 — Generar `index.md`

Leer todos los archivos de `.agents/` y generar `.agents/index.md` con este formato:

```markdown
# Índice — Contexto del proyecto para Claude

## Resumen del proyecto
(10 líneas máximo: qué es, qué problema resuelve, stack principal)

## Archivos de contexto
- [context.md](./context.md) — arquitectura, módulos, relaciones clave
- [design/architecture.md](./design/architecture.md) — estructura general
- [design/auth.md](./design/auth.md) — diseño de autenticación
- [design/database.md](./design/database.md) — modelo de datos
- [design/methodology.md](./design/methodology.md) — metodología del proyecto
- [sprint/current.md](./sprint/current.md) — sprint activo

## Convenciones
- [conventions/diagrams.md](./conventions/diagrams.md) — cómo generar diagramas
- [conventions/documentation-guidelines.md](./conventions/documentation-guidelines.md) — reglas de documentación
```

---

## Paso 4 — Generar `context.md`

Leer `PROJECT_CONTEXT.md` y todos los archivos de `.agents/design/` y generar
`.agents/context.md` con este formato:

```markdown
# Contexto del proyecto

## Stack
(lenguajes, frameworks, BD, herramientas principales)

## Módulos principales
(lista corta con una línea por módulo: nombre → qué hace)

## Relaciones clave entre módulos
(qué depende de qué, flujos importantes)

## Restricciones de negocio
(reglas que Claude debe respetar al generar código)

## Antipatrones conocidos
(qué NO hacer en este proyecto)

## Comandos frecuentes
(cómo correr, testear, seed, build)
```

---

## Reglas para Claude al ejecutar esta tarea

- No inventar contenido — solo reorganizar y sintetizar lo que ya existe
- Si un archivo tiene contenido que no encaja en ninguna categoría, crear una sección "Otros" en `context.md`
- Si `DOCUMENTATION.md` tiene contenido único que no está en `documentation-guidelines.md`, preservarlo
- Los `.svg` y `.d2` de diagramas van en `docs/diagrams/`, no en `.agents/`
- No modificar los archivos de `conventions/` — ya están correctos
- Confirmar cada movimiento antes de ejecutar si hay dudas sobre dónde va algo
