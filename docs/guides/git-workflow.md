# Flujo de trabajo en Git — Banco Digital

> **Integrantes:** mista, mafe, bryan, xiomi, cristian

---

## Estructura de ramas

```
main                    ← producción (solo recibe merges desde develop al final del sprint)
└── develop             ← integración del equipo (rama base de trabajo diario)
    ├── mista/nombre-hu
    ├── mafe/nombre-hu
    ├── bryan/nombre-hu
    ├── xiomi/nombre-hu
    └── cristian/nombre-hu
```

- **`main`** representa el estado desplegable del sistema. Nunca se trabaja directamente sobre ella.
- **`develop`** es la rama donde se integra el trabajo de todos. Es la base desde la que cada integrante crea su rama.
- Las **ramas personales** corresponden a una Historia de Usuario (HU) o tarea concreta. Se crean desde `develop` y se integran de vuelta a `develop` mediante PR.

---

## Ciclo completo de trabajo por Historia de Usuario

### 1. Antes de empezar — actualizar develop

Siempre partir desde `develop` actualizado para evitar conflictos desde el inicio:

```bash
git checkout develop
git pull origin develop
```

### 2. Crear la rama de la HU

El nombre sigue el patrón `{usuario}/{descripcion-corta}`:

```bash
git checkout -b mista/feat-login-usuario
```

Ejemplos válidos de nombres de ramas:
```
mista/feat-login-usuario
mafe/feat-registro-cliente
bryan/fix-calculo-saldo
xiomi/feat-listar-cuentas
cristian/refactor-transacciones
```

### 3. Desarrollar la HU y hacer commits frecuentes

Hacer commits pequeños y descriptivos a medida que se avanza. No acumular todo el trabajo en un solo commit al final.

```bash
git add src/main/java/fe/banco_digital/service/UsuarioServiceImpl.java
git commit -m "feat(usuario): agregar validación de credenciales en login"

git add src/main/java/fe/banco_digital/controller/UsuarioController.java
git commit -m "feat(usuario): exponer endpoint POST /api/v1/auth/login"
```

#### Formato de commits

```
feat(modulo):     nueva funcionalidad
fix(modulo):      corrección de un bug
refactor(modulo): mejora de código sin cambiar comportamiento
test(modulo):     agregar o modificar tests
docs(modulo):     cambios en documentación
```

El `modulo` es el nombre del recurso afectado en minúscula: `usuario`, `cuenta`, `transaccion`, `cliente`, `auth`.

### 4. Sincronizar con develop cada 1-2 días

Mientras trabajas en tu rama, el resto del equipo puede haber integrado cambios en `develop`. Traerlos a tu rama regularmente reduce los conflictos:

```bash
# Ir a develop y actualizarlo
git checkout develop
git pull origin develop

# Volver a tu rama y traer los cambios
git checkout mista/feat-login-usuario
git merge develop
```

Si hay conflictos, resolverlos en los archivos marcados por Git, luego:

```bash
git add .
git commit -m "merge: integrar cambios de develop en feat-login-usuario"
```

### 5. Subir la rama y abrir Pull Request hacia develop

Cuando la HU esté lista y probada:

```bash
git push origin mista/feat-login-usuario
```

Luego abrir un **Pull Request (PR)** en GitHub:
- **Base:** `develop`
- **Compare:** `mista/feat-login-usuario`
- **Título:** breve descripción de lo que se implementó
- **Descripción:** qué endpoints se agregaron, qué lógica cambió, cómo probar manualmente

### 6. Revisión y aprobación del PR

- **Mínimo 1 integrante del equipo** debe revisar y aprobar el PR antes del merge.
- El revisor verifica que se cumpla el checklist (ver abajo).
- Si hay correcciones, el autor las hace en la misma rama y vuelve a pushear — el PR se actualiza automáticamente.

### 7. Merge a develop

Una vez aprobado, se hace el merge a `develop`. Preferiblemente con **"Squash and merge"** o **"Merge commit"** según el tamaño del PR — el equipo decide la estrategia y la mantiene consistente.

### 8. Merge de develop a main (al final del sprint)

Al cerrar un sprint, el líder o el equipo hace el merge de `develop` a `main`:

```bash
git checkout main
git pull origin main
git merge develop
git push origin main
```

Esto solo ocurre cuando `develop` está estable y el sprint está completado. Nunca se hace merge directo de una rama personal a `main`.

---

## Diagrama del flujo

```
develop ──────────────────────────────────────────────────────► develop
   │                                                               ▲
   │  git checkout -b mista/feat-login                            │
   ▼                                                               │
mista/feat-login                                                   │
   │  commit: "feat(auth): agregar validación"                    │
   │  commit: "feat(auth): exponer endpoint login"                │
   │  merge develop (sync diaria)                                  │
   │                                                               │
   └──────────────────── PR aprobado ─────────────────────────────┘
                                                                   │
                                                               merge a main
                                                           (fin de sprint)
                                                                   ▼
                                                                 main
```

---

## Reglas que no se negocian

| Regla | Razón |
|---|---|
| Nunca trabajar directamente en `main` o `develop` | Evitar romper el código integrado del equipo |
| Siempre partir desde `develop` actualizado | Reducir conflictos desde el inicio |
| PR siempre hacia `develop`, nunca a `main` | `main` solo recibe merges controlados al cerrar el sprint |
| Mínimo 1 aprobación antes del merge | Garantizar que alguien más leyó el código |
| No hacer merge de un PR con endpoints sin Swagger | La API debe estar documentada antes de integrarse |
| No hacer merge si el código no compila | El equipo no puede avanzar sobre código roto |

---

## Checklist antes de abrir un PR

- [ ] El código compila sin errores (`./mvnw package -DskipTests`)
- [ ] Probé manualmente que el endpoint funciona
- [ ] No hay credenciales ni contraseñas en el código
- [ ] Sincronicé con `develop` antes de hacer push (resolví conflictos si los había)
- [ ] Usé `BigDecimal` para valores monetarios
- [ ] Los endpoints siguen el patrón `/api/v1/...`
- [ ] Ningún endpoint recibe ni devuelve una entidad — usé DTOs
- [ ] La conversión entidad ↔ DTO la hace el Mapper
- [ ] Los endpoints están documentados con Swagger (`@Tag`, `@Operation`, `@ApiResponses`)
- [ ] El PR tiene título claro y descripción de qué se hizo y cómo probarlo

---

## Resolución de conflictos

Los conflictos ocurren cuando dos personas modificaron el mismo archivo. Git los marca así:

```
<<<<<<< HEAD (tu rama)
public String getNombreCompleto() {
    return nombre + " " + apellido;
}
=======
public String getNombreCompleto() {
    return apellido + ", " + nombre;
}
>>>>>>> develop
```

Pasos para resolverlos:
1. Abrir el archivo en conflicto.
2. Decidir qué versión queda (o combinar ambas si tiene sentido).
3. Eliminar las marcas `<<<<<<<`, `=======`, `>>>>>>>`.
4. Hacer `git add` del archivo y luego el commit de merge.

Si el conflicto es complejo, consultar con quien hizo el otro cambio antes de decidir.
