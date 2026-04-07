# Documentation Guidelines

## Estructura general

```
proyecto/
├── CLAUDE.md                          ← punto de entrada para Claude
├── .agents/                           ← exclusivo para IA (Claude Code)
│   
└── docs/                              ← exclusivo para humanos
    ├── index.md
    ├── architecture.md
    ├── modules/
    ├── guides/
    └── diagrams/
```

**Regla absoluta:** ningún archivo de `docs/` debe contener instrucciones para Claude,
y ningún archivo de `.agents/` debe estar escrito pensando en humanos.

---

## PARTE 1 — Documentación para humanos (`docs/`)

### Principios

- Detallada pero concisa: explicar el qué y el por qué, no el cómo implementado
- Escrita para un desarrollador que llega nuevo al proyecto
- Actualizada en el mismo commit o PR donde se agrega funcionalidad nueva
- Nunca documentar código autoexplicativo

### Herramientas

#### Diagramas — D2 con tema Terrastruct

Todos los diagramas se crean en D2 y se renderizan como `.svg`.

**Instalación:**
```bash
brew install d2         # macOS
curl -fsSL https://d2lang.com/install.sh | sh  # Linux
```

**Renderizado:**
```bash
d2 --theme 200 --layout elk docs/diagrams/nombre.d2 docs/diagrams/nombre.svg
```

**Renderizar todos:**
```bash
for f in docs/diagrams/*.d2; do
  d2 --theme 200 --layout elk "$f" "${f%.d2}.svg"
done
```

**Tipos de diagrama por situación:**

| Situación | Tipo |
|---|---|
| Módulos y relaciones | Componentes |
| Flujo de una petición | Sequence diagram |
| Estados de una entidad | State diagram |
| Estructura de BD | Entity-relationship |
| Infraestructura | Network diagram |

**Ejemplo D2:**
```d2
direction: right

client: Cliente {shape: rectangle}
api: API Gateway {shape: hexagon}
auth: Auth Service {shape: rectangle}
db: PostgreSQL {shape: cylinder}

client -> api: HTTP + JWT
api -> auth: Verificar token
api -> db: Query
```

#### Documentación navegable — Mintlify o Docusaurus

Para cuando el equipo crezca y se necesite documentación pública o navegable:

- **Mintlify** → recomendado, sincroniza desde el repo, soporte MDX, diseño profesional out of the box
- **Docusaurus** → más flexible, permite componentes React en los `.md`

Mientras el proyecto sea pequeño, los `.md` vinculados con `.svg` embebidos son suficientes.

---

### Estructura de `docs/`

```
docs/
├── index.md                  ← índice general con links a todo
├── architecture.md           ← visión general del sistema
├── modules/
│   ├── auth.md
│   ├── accounts.md
│   ├── transactions.md
│   └── ...
├── guides/
│   ├── getting-started.md    ← cómo correr el proyecto
│   ├── seed.md               ← cómo cargar datos de prueba
│   └── api-usage.md          ← cómo consumir la API
├── decisions/
│   ├── adr-001-jwt.md
│   └── ...
└── diagrams/
    ├── architecture.d2
    ├── architecture.svg
    ├── auth-flow.d2
    ├── auth-flow.svg
    └── ...
```

---

### Plantilla para documentar un módulo

```markdown
# Nombre del módulo

## ¿Qué hace?
Una o dos frases. Sin jerga técnica innecesaria.

## Responsabilidades
- Responsabilidad 1
- Responsabilidad 2

## Lo que NO hace
(evita que otros pongan cosas aquí que no corresponden)

## Diagrama
![Nombre](../diagrams/nombre-modulo.svg)

## Endpoints principales
| Método | Ruta | Descripción |
|--------|------|-------------|
| POST | /api/auth/login | Autenticación |

## Cómo se usa
Ejemplo concreto con código real del proyecto.

## Dependencias
| Módulo | Para qué |
|--------|----------|
| Auth | Verificar permisos |

## Decisiones relevantes
- [ADR-001 — Por qué JWT](../decisions/adr-001-jwt.md)
```

---

### Plantilla ADR

Crear un ADR cada vez que se tome una decisión técnica importante o no obvia.

**Cuándo crear uno:**
- Se elige una librería sobre otra
- Se define un patrón que todos deben seguir
- Se descarta una alternativa que parece obvia
- Se cambia una decisión anterior

```markdown
# ADR-XXX: Título corto

**Estado:** Propuesta | Aceptada | Deprecada | Reemplazada por ADR-YYY
**Fecha:** YYYY-MM-DD

## Contexto
¿Qué situación originó esta decisión?

## Opciones consideradas
1. Opción A — pro / contra
2. Opción B — pro / contra

## Decisión
Qué se eligió y por qué.

## Consecuencias
Qué implica a futuro.
```

Nombrar archivos: `adr-001-descripcion-corta.md`

---

### Actualización automática

**Regla del proyecto:** ningún PR que agregue una funcionalidad nueva puede mergearse
sin actualizar o crear el `.md` correspondiente en `docs/`.

**Checklist antes de hacer commit:**
- [ ] ¿Agregué o modifiqué un módulo? → actualizar `docs/modules/`
- [ ] ¿Agregué un endpoint nuevo? → actualizar la tabla de endpoints del módulo
- [ ] ¿Cambié la arquitectura? → regenerar el diagrama D2 y el `.svg`
- [ ] ¿Tomé una decisión técnica importante? → crear ADR en `docs/decisions/`
- [ ] ¿Cambié cómo se corre el proyecto? → actualizar `docs/guides/`

---

### Tono y estilo

- Idioma del equipo: español (no mezclar)
- Párrafos cortos, máximo 4 líneas
- Preferir listas sobre bloques de texto densos
- Ejemplos reales del proyecto, no abstractos
- Evitar: "simplemente", "obviamente", "es fácil"
- No documentar TODOs ni deuda técnica → esos van en issues del repo

---

## PARTE 2 — Contexto para IA (`.agents/`)

### Principio

`.agents/` contiene solo lo que Claude necesita para entender el proyecto y actuar correctamente.
No es documentación — es contexto operativo. No debe estar escrito para ser leído por humanos.

### Qué incluir

- Comandos frecuentes del proyecto
- Decisiones de arquitectura no obvias que afectan cómo Claude debe generar código
- Patrones obligatorios y antipatrones conocidos
- Relaciones entre módulos que no son evidentes en el código
- Restricciones de negocio que Claude debe respetar

### Qué NO incluir

- Explicaciones narrativas largas
- Documentación de API pública
- Guías de onboarding
- Cualquier cosa escrita pensando en que la lea un humano

### Estructura de `.agents/`

```
.agents/
├── index.md              ← punto de entrada, resumen del proyecto en 10 líneas
├── context.md            ← arquitectura, módulos, relaciones clave
├── conventions/
│   ├── diagrams.md       ← cómo generar diagramas D2
│   ├── documentation.md  ← este archivo
│   └── code.md           ← patrones de código obligatorios
└── decisions/
    └── adr-001-jwt.md    ← decisiones que afectan cómo Claude genera código
```
se pueden agregar mas carpetas y demas buscando la mejor organizacion sin redundar.
Aqui solo pueden haber archivos .md y carpetas
### Formato para Claude

Directo y sin narrativa. Ejemplo:

```markdown
## Auth
- JWT con refresh token
- Filtro en FiltroJwt.java — NO modificar sin revisar SecurityConfig
- Roles: ADMIN, CLIENTE — definidos en RolNombre.java
- Usuario.roles tiene fetch EAGER — no cambiar a LAZY
```

No así:
```markdown
## Módulo de autenticación
El módulo de autenticación es responsable de gestionar el acceso de los usuarios
al sistema mediante tokens JWT...
```
