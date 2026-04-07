# Convenciones de Diagramas

## Herramienta principal: D2

Siempre usar **D2** para crear diagramas nuevos. No usar Mermaid salvo que el diagrama ya exista en ese formato.

### Instalación (si no está disponible)
```bash
brew install d2         # macOS
# o
curl -fsSL https://d2lang.com/install.sh | sh
```

### Renderizado estándar
```bash
d2 --theme 200 --layout elk archivo.d2 archivo.svg
```

- `--theme 200` → tema Terrastruct (limpio, profesional)
- `--layout elk` → mejor distribución automática de nodos
- Siempre exportar a `.svg` para que se embeba correctamente en `.md`

### Renderizar todos los diagramas de una vez
```bash
for f in docs/diagrams/*.d2; do
  d2 --theme 200 --layout elk "$f" "${f%.d2}.svg"
done
```

---

## Estructura de archivos

Los diagramas viven en `docs/diagrams/`, no aquí. Claude los **genera**, los humanos los **consumen**.

```
docs/
└── diagrams/
    ├── architecture.d2       ← fuente editable
    ├── architecture.svg      ← generado, no editar a mano
    ├── auth-flow.d2
    ├── auth-flow.svg
    └── ...
```

- El `.d2` es el fuente → se versiona y se edita
- El `.svg` es el output → se regenera con el comando de arriba
- Nunca editar el `.svg` manualmente

---

## Cómo referenciar en `.md`

```markdown
![Arquitectura general](../diagrams/architecture.svg)
```

Usar siempre ruta relativa desde el archivo `.md` que lo referencia.

---

## Tipos de diagrama y cuándo usarlos

| Situación | Tipo D2 |
|---|---|
| Módulos y sus relaciones | Diagrama de componentes |
| Flujo de una petición HTTP | Sequence diagram |
| Estados de una entidad | State diagram |
| Estructura de base de datos | Entity-relationship |
| Infraestructura / deploy | Network diagram |

---

## Ejemplo base D2

```d2
# Arquitectura de ejemplo
direction: right

client: Cliente {shape: rectangle}
api: API Gateway {shape: hexagon}
auth: Auth Service {shape: rectangle}
db: Base de Datos {shape: cylinder}

client -> api: HTTP Request
api -> auth: Verificar token
api -> db: Query
```

---

## Reglas

- Un diagrama por archivo `.d2`
- Nombre del archivo en `kebab-case` y en inglés
- Incluir un comentario `#` al inicio describiendo qué representa
- Regenerar el `.svg` siempre que se modifique el `.d2`
- No crear diagramas en herramientas externas — todo vive en el repo
