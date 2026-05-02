# Banco Digital — Resumen completo para diseño de frontend

## ¿Qué es el sistema?

Backend REST de un banco digital construido con Java 17 + Spring Boot 3 + PostgreSQL. Permite a clientes registrarse, autenticarse, ver sus cuentas y movimientos, hacer depósitos, retiros y transferencias, y recibir depósitos desde pasarelas de pago externas.

---

## Autenticación

El sistema usa **JWT en cookies HttpOnly**. No se usan headers `Authorization: Bearer` desde el frontend — los tokens van y vienen automáticamente en cookies del navegador.

### Flujo completo

```
1. POST /api/v1/auth/login
   → El servidor setea dos cookies HttpOnly en la respuesta:
     - accessToken  (duración: 10 minutos)
     - refreshToken (duración: 7 días, solo accesible en /api/v1/auth/refresh)

2. Todas las peticiones autenticadas llevan las cookies automáticamente (el navegador las incluye solo).

3. Cuando el accessToken expira:
   POST /api/v1/auth/refresh
   → El servidor usa el refreshToken de la cookie para emitir un nuevo par de tokens.

4. POST /api/v1/auth/logout
   → El servidor borra las cookies y revoca el refreshToken.
```

### Rutas públicas (sin autenticación)
- `POST /api/v1/auth/login`
- `POST /api/v1/auth/registro` (registro nuevo usuario — sin crear cuenta nueva, para clientes ya existentes en el banco)
- `POST /api/v1/registro` (registro completo nuevo cliente)
- `POST /api/v1/depositos/notificacion` (solo pasarelas autorizadas por HMAC)

### Rutas protegidas
Todas las demás. Si no hay cookie válida → `401 Unauthorized`.

---

## Módulos y pantallas sugeridas

| Módulo | Pantallas |
|--------|-----------|
| Autenticación | Login, Registro |
| Dashboard | Lista de cuentas con saldos |
| Transacciones | Depositar, Retirar, Transferir, Historial |
| Perfil / Cliente | Ver y editar datos personales |
| Seguridad de cuenta | Bloquear / Desbloquear cuenta, Cerrar cuenta |

---

## Endpoints por módulo

### AUTENTICACIÓN — `/api/v1/auth`

#### POST `/api/v1/auth/login`
```json
// Request
{ "username": "bryan", "password": "bryan123" }

// Response 200 — setea cookies, body solo confirmación
{ "mensaje": "Sesión iniciada exitosamente" }

// Response 401
{ "timestamp": "...", "estado": 401, "error": "Unauthorized", "mensaje": "Credenciales inválidas." }
```

#### POST `/api/v1/auth/logout`
```json
// Request — sin body
// Response 200
{ "mensaje": "Sesión cerrada exitosamente" }
```

#### POST `/api/v1/auth/refresh`
```json
// Request — sin body (usa cookie refreshToken automáticamente)
// Response 200 — emite nuevas cookies
{ "mensaje": "Token renovado exitosamente" }

// Response 401 — refresh token expirado
{ "mensaje": "Token inválido o expirado." }
```

---

### REGISTRO — `/api/v1/registro`

#### POST `/api/v1/registro/validar-identidad`
Paso previo al registro: verifica que el documento no esté ya registrado.
```json
// Request
{ "documento": "1032456789", "fechaExpedicion": "2022-05-10" }

// Response 200
{ "disponible": true, "mensaje": "Identidad disponible para continuar con el registro." }

// Response 409 — ya existe
{ "mensaje": "La identificación ya está vinculada a una cuenta existente." }
```

#### POST `/api/v1/registro`
Crea cliente + usuario + cuenta de ahorros automáticamente.
```json
// Request
{
  "documento": "1032456789",
  "fechaExpedicion": "2022-05-10",
  "nombre": "Juan Pérez",
  "email": "juan@example.com",
  "direccion": "Calle 50 #40-20",
  "telefono": "3001234567",
  "username": "juanp01",
  "password": "clave123"
}

// Response 201
{
  "idCliente": 7,
  "idUsuario": 7,
  "idCuenta": 8,
  "numeroCuenta": "48392017",
  "saldo": 0,
  "mensaje": "Cliente registrado exitosamente, junto al número de cuenta y saldo."
}

// Response 400 — campos inválidos
{
  "timestamp": "...", "estado": 400, "error": "Bad Request",
  "mensaje": "Error de validación",
  "errores": ["El nombre es obligatorio.", "El email no tiene formato válido."]
}

// Response 409 — documento, email o username duplicados
{ "mensaje": "La identificación ya está vinculada a una cuenta existente." }
```

---

### DASHBOARD — `/api/v1/cuentas`

#### GET `/api/v1/cuentas/dashboard` 🔒
Lista todas las cuentas del cliente autenticado.
```json
// Response 200
[
  {
    "idCuenta": 1,
    "numeroCuenta": "00010001",
    "numeroEnmascarado": "****0001",
    "tipo": "AHORROS",
    "saldo": 1225000.0000,
    "saldoDisponible": true,
    "estado": "ACTIVA",
    "permiteTransacciones": true,
    "etiquetaVisual": null
  },
  {
    "idCuenta": 3,
    "numeroCuenta": "00010003",
    "numeroEnmascarado": "****0003",
    "tipo": "AHORROS",
    "saldo": 0.0000,
    "saldoDisponible": true,
    "estado": "INACTIVA",
    "permiteTransacciones": false,
    "etiquetaVisual": "Cuenta Cerrada"
  }
]
```

> `saldoDisponible: false` indica que el servicio de saldos no respondió — mostrar "Dato no disponible" en lugar del monto y ofrecer botón "Reintentar".

---

### TRANSACCIONES — `/api/v1/transacciones` 🔒

#### POST `/api/v1/transacciones/depositar`
```json
// Request
{ "idCuenta": 1, "monto": 50000.00 }

// Response 200
{
  "idTransaccion": 10,
  "tipo": "DEPOSITO",
  "monto": 50000.00,
  "saldoResultante": 1275000.00,
  "fecha": "2026-04-30T14:22:00",
  "estado": "EXITOSA"
}

// Response 409 — cuenta bloqueada o inactiva
{ "mensaje": "La cuenta está bloqueada y no puede operar." }

// Response 404 — cuenta no encontrada o no pertenece al usuario
{ "mensaje": "Cuenta no encontrada." }
```

#### POST `/api/v1/transacciones/retirar`
```json
// Request
{ "idCuenta": 1, "monto": 30000.00 }

// Response 200 — igual que depósito
// Response 409 — saldo insuficiente
{ "mensaje": "Saldo insuficiente para realizar el retiro." }
```

#### POST `/api/v1/transacciones/transferir`
```json
// Request
{
  "idCuentaOrigen": 1,
  "numeroCuentaDestino": "00020001",
  "monto": 20000.00
}

// Response 200
{
  "idTransaccion": 11,
  "tipo": "TRANSFERENCIA",
  "monto": 20000.00,
  "saldoResultante": 1205000.00,
  "fecha": "2026-04-30T14:25:00",
  "estado": "EXITOSA"
}

// Response 409 — saldo insuficiente
// Response 404 — cuenta origen o destino no encontrada
```

#### GET `/api/v1/transacciones/cuenta/{idCuenta}`
Lista el historial de movimientos de una cuenta.
```json
// Response 200
[
  {
    "idTransaccion": 10,
    "tipo": "DEPOSITO",
    "monto": 50000.00,
    "fecha": "2026-04-30T14:22:00",
    "estado": "EXITOSA",
    "numeroCuentaOrigen": null,
    "numeroCuentaDestino": "00010001"
  },
  {
    "idTransaccion": 11,
    "tipo": "TRANSFERENCIA",
    "monto": 20000.00,
    "fecha": "2026-04-30T14:25:00",
    "estado": "EXITOSA",
    "numeroCuentaOrigen": "00010001",
    "numeroCuentaDestino": "00020001"
  }
]
```

#### GET `/api/v1/transacciones/cuenta/{idCuenta}/filtro?desde=2026-01-01&hasta=2026-04-30`
Mismo formato, filtrado por rango de fechas.

---

### PERFIL / CLIENTE — `/api/v1/clientes` 🔒

#### PUT `/api/v1/clientes/me`
Actualiza solo teléfono y email. El nombre, documento y número de cuenta son de solo lectura.
```json
// Request
{ "telefono": "3009876543", "email": "nuevo@email.com" }

// Response 200
{ "mensaje": "Datos actualizados exitosamente." }

// Response 409 — email ya existe en otro cliente
{ "mensaje": "El correo ya está registrado." }
```

---

### SEGURIDAD DE CUENTA — `/api/v1/cuentas/seguridad` 🔒

Todos los endpoints requieren la **contraseña del usuario** para confirmar la operación.

#### POST `/api/v1/cuentas/seguridad/bloquear`
```json
// Request
{ "idCuenta": 1, "contrasena": "bryan123" }

// Response 200
{ "mensaje": "Cuenta bloqueada exitosamente." }

// Response 401 — contraseña incorrecta
{ "mensaje": "Autenticación fallida." }
```

#### POST `/api/v1/cuentas/seguridad/desbloquear`
```json
// Request
{ "idCuenta": 1, "contrasena": "bryan123" }

// Response 200
{ "mensaje": "Cuenta desbloqueada exitosamente." }
```

#### PATCH `/api/v1/cuentas/cerrar`
Solo permite cerrar si el saldo es cero. Irreversible.
```json
// Request
{ "idCuenta": 1, "contrasena": "bryan123" }

// Response 200
{
  "numeroCuenta": "00010001",
  "estado": "INACTIVA",
  "mensaje": "El cierre de tu cuenta ha sido realizado exitosamente."
}

// Response 409 — saldo pendiente
{ "mensaje": "No se puede cerrar la cuenta porque tiene saldo pendiente." }

// Response 400 — cuenta ya cerrada
{ "mensaje": "La cuenta ya está cerrada." }
```

---

## Modelo de datos — lo que el frontend necesita conocer

### Usuario / Sesión
| Campo | Notas |
|-------|-------|
| `username` | Identificador de login |
| No se expone `id` directamente | El backend lo infiere del token |

### Cliente (datos personales)
| Campo | Editable |
|-------|----------|
| `nombre` | No |
| `documento` | No |
| `email` | Sí |
| `telefono` | Sí |
| `direccion` | No (en el sprint actual) |

### Cuenta
| Campo | Descripción |
|-------|-------------|
| `idCuenta` | Usar para operaciones (depósito, retiro, etc.) |
| `numeroEnmascarado` | Mostrar en UI (`****1234`) |
| `numeroCuenta` | No mostrar directamente — usar para transferencias entrantes |
| `tipo` | `AHORROS` o `CORRIENTE` |
| `saldo` | `BigDecimal` — formatear como moneda COP |
| `estado` | `ACTIVA` / `INACTIVA` / `BLOQUEADA` |
| `permiteTransacciones` | `true` solo si `ACTIVA` — deshabilitar botones de operación si `false` |
| `etiquetaVisual` | `"Cuenta Cerrada"` o `null` |

### Transacción
| Campo | Descripción |
|-------|-------------|
| `tipo` | `DEPOSITO` / `RETIRO` / `TRANSFERENCIA` |
| `estado` | `EXITOSA` / `FALLIDA` |
| `monto` | `BigDecimal` |
| `numeroCuentaOrigen` | `null` en depósitos |
| `numeroCuentaDestino` | `null` en retiros |

---

## Estados y transiciones de cuenta

```
        bloquear (con contraseña)
ACTIVA ─────────────────────────▶ BLOQUEADA
ACTIVA ◀───────────────────────── BLOQUEADA
        desbloquear (con contraseña)

        cerrar (saldo = 0, con contraseña)
ACTIVA ─────────────────────────▶ INACTIVA  (irreversible)
```

---

## Catálogo de errores para el frontend

| HTTP | Cuándo mostrar |
|------|---------------|
| `400 Bad Request` | Formulario con campos inválidos o vacíos — mostrar mensajes por campo |
| `401 Unauthorized` | Sesión expirada → redirigir al login / contraseña incorrecta |
| `403 Forbidden` | Intentó operar sobre una cuenta que no le pertenece |
| `404 Not Found` | Cuenta o recurso no existe |
| `409 Conflict` | Saldo insuficiente, cuenta bloqueada, duplicado (email/username/documento) |
| `422 Unprocessable Entity` | Depósito de pasarela rechazado (solo gateway, no aplica al frontend de cliente) |
| `500 Internal Server Error` | Error inesperado del servidor — mostrar mensaje genérico |

### Estructura de error estándar
```json
{
  "timestamp": "2026-04-30T14:33:46.421783181",
  "estado": 409,
  "error": "Conflict",
  "mensaje": "Saldo insuficiente para realizar el retiro."
}
```

### Estructura de error de validación (400)
```json
{
  "timestamp": "...",
  "estado": 400,
  "error": "Bad Request",
  "mensaje": "Error de validación",
  "errores": [
    "El monto es obligatorio.",
    "El número de cuenta destino es obligatorio."
  ]
}
```

---

## Notas técnicas para el frontend

- **Moneda**: todos los valores monetarios son `BigDecimal`. Formatear como COP (pesos colombianos).
- **Fechas**: formato ISO-8601 (`2026-04-30T14:33:46`). Mostrar en zona horaria local.
- **Cookies**: no manipular manualmente. El navegador las gestiona solas si el frontend está en el mismo dominio o con CORS configurado.
- **CORS**: el backend acepta peticiones de `http://localhost:3000` y `http://localhost:5173` por defecto.
- **`idCuenta` vs `numeroCuenta`**: usar `idCuenta` (Long) para todas las operaciones propias del usuario. Usar `numeroCuenta` (String) solo cuando el usuario escribe el número destino de una transferencia.
- **Reintentar sesión**: cuando el servidor devuelve `401`, intentar `POST /api/v1/auth/refresh` automáticamente antes de redirigir al login.
- **Saldo no disponible**: si `saldoDisponible: false` en el dashboard, mostrar "Dato no disponible" con botón de recarga — no mostrar `0` ni `null`.
