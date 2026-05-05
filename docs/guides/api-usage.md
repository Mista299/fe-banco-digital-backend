# Consumir la API

Base URL: `http://localhost:8080`

Documentación interactiva: `http://localhost:8080/swagger-ui.html`

---

## Autenticación

Todas las rutas protegidas requieren el header:

```
Authorization: Bearer <accessToken>
```

### 1. Registrar usuario

```http
POST /api/v1/auth/registro
Content-Type: application/json

{
  "username": "mafe99",
  "password": "miClave123",
  "idCliente": 2
}
```

### 2. Login

```http
POST /api/v1/auth/login
Content-Type: application/json

{
  "username": "mafe99",
  "password": "miClave123"
}
```

Respuesta:
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "refreshToken": "550e8400-e29b-41d4-a716-446655440000",
  "tipo": "Bearer",
  "expiraEn": 600000
}
```

### 3. Renovar token

```http
POST /api/v1/auth/refresh
Content-Type: application/json

{ "refreshToken": "550e8400-..." }
```

### 4. Cerrar sesión

```http
POST /api/v1/auth/logout
Content-Type: application/json

{ "refreshToken": "550e8400-..." }
```

---

## Datos que el servidor extrae del token (no enviar en el request)

El backend extrae el `username` del JWT y desde ahí resuelve el `idCliente` y las cuentas del usuario. **Nunca se debe enviar el ID del usuario ni del cliente en el body o en la URL** para operaciones sobre los propios recursos. Hacerlo no tiene efecto (se ignora) o devuelve `403 Forbidden` si el ID no coincide con el token.

| Endpoint | El servidor deriva del token |
|----------|------------------------------|
| `PUT /api/v1/clientes/me` | `idCliente` completo |
| `GET /api/v1/cuentas/dashboard` | `idCliente` → lista todas sus cuentas |
| `POST /api/v1/cuentas/seguridad/bloquear` | cuenta activa del cliente |
| `POST /api/v1/cuentas/seguridad/desbloquear` | cuenta bloqueada del cliente |
| `POST /api/v1/transacciones/depositar` | `idCliente` para verificar que la cuenta enviada le pertenece |
| `POST /api/v1/transacciones/retirar` | `idCliente` para verificar que la cuenta enviada le pertenece |
| `POST /api/v1/transacciones/transferir` | `idCliente` para verificar que la cuenta origen le pertenece |
| `GET /api/v1/transacciones/cuenta/{idCuenta}` | `idCliente` para verificar que la cuenta le pertenece |

---

## Clientes

### Actualizar datos del cliente autenticado

No se envía ningún ID. El servidor identifica al cliente por el token.

```http
PUT /api/v1/clientes/me
Authorization: Bearer eyJ...
Content-Type: application/json

{
  "email": "nuevo@email.com",
  "telefono": "3217654321"
}
```

Respuesta `200`:
```json
{ "mensaje": "Tus datos se han actualizado correctamente" }
```

---

## Cuentas

### Ver todas las cuentas del usuario autenticado

```http
GET /api/v1/cuentas/dashboard
Authorization: Bearer eyJ...
```

Respuesta `200`:
```json
[
  {
    "numeroCuenta": "5001000001",
    "tipo": "AHORROS",
    "saldo": 150000.0000,
    "estado": "ACTIVA"
  }
]
```

### Cerrar cuenta

Requiere saldo cero y confirmación de contraseña. El servidor verifica que la cuenta pertenezca al cliente del token.

```http
PATCH /api/v1/cuentas/cerrar
Authorization: Bearer eyJ...
Content-Type: application/json

{
  "idCuenta": 1,
  "contrasena": "miClave123"
}
```

Respuesta `200`:
```json
{
  "numeroCuenta": "5001000001",
  "estado": "INACTIVA",
  "mensaje": "El cierre de tu cuenta ha sido realizado exitosamente."
}
```

### Bloquear cuenta

Bloquea la cuenta activa del usuario autenticado. El servidor resuelve la cuenta desde el token — no se envía `idCuenta`.

```http
POST /api/v1/cuentas/seguridad/bloquear
Authorization: Bearer eyJ...
Content-Type: application/json

{ "password": "miClave123" }
```

Respuesta `200`: `"Cuenta bloqueada exitosamente"`

### Desbloquear cuenta

```http
POST /api/v1/cuentas/seguridad/desbloquear
Authorization: Bearer eyJ...
Content-Type: application/json

{ "password": "miClave123" }
```

Respuesta `200`: `"Cuenta desbloqueada exitosamente"`

---

## Transacciones

En todos los endpoints de transacciones se envía `idCuenta` porque el cliente puede tener varias cuentas y debe elegir sobre cuál operar. El servidor siempre verifica que la cuenta pertenezca al cliente del token; si no, devuelve `403 Forbidden`.

### Depositar

```http
POST /api/v1/transacciones/depositar
Authorization: Bearer eyJ...
Content-Type: application/json

{
  "idCuenta": 1,
  "monto": 50000.00
}
```

Respuesta `200`:
```json
{
  "idTransaccion": 10,
  "tipo": "DEPOSITO",
  "monto": 50000.0000,
  "saldoResultante": 200000.0000,
  "estado": "EXITOSA",
  "fecha": "2026-04-28T22:00:00",
  "mensaje": "Depósito realizado exitosamente."
}
```

### Retirar

```http
POST /api/v1/transacciones/retirar
Authorization: Bearer eyJ...
Content-Type: application/json

{
  "idCuenta": 1,
  "monto": 10000.00
}
```

Respuesta `200`: misma estructura que depositar con `"tipo": "RETIRO"`.

Respuesta `409` si saldo insuficiente:
```json
{ "mensaje": "Saldo insuficiente para realizar la operación." }
```

### Transferir

```http
POST /api/v1/transacciones/transferir
Authorization: Bearer eyJ...
Content-Type: application/json

{
  "idCuentaOrigen": 1,
  "numeroCuentaDestino": "5001000002",
  "monto": 25000.00
}
```

Respuesta `200`: misma estructura que depositar con `"tipo": "TRANSFERENCIA"`. El `saldoResultante` refleja el saldo de la cuenta origen tras la operación.

### Ver movimientos de una cuenta

```http
GET /api/v1/transacciones/cuenta/1
Authorization: Bearer eyJ...
```

### Filtrar movimientos por fechas

```http
GET /api/v1/transacciones/cuenta/1/filtro?fechaInicio=2026-04-01T00:00:00&fechaFin=2026-04-30T23:59:59
Authorization: Bearer eyJ...
```

---

## Perfil del cliente (legado)

```http
GET /api/v1/profile/{userId}
Authorization: Bearer eyJ...
```

> Este endpoint recibe `userId` en la URL. Está marcado como legado — los endpoints nuevos derivan la identidad del token sin requerir ID en la ruta.

---

## Verificar conectividad con la BD

```http
GET /api/db-ping
```

Respuesta: `{"status":"ok"}`

---

## Códigos de respuesta

| Código | Significado |
|--------|-------------|
| `200` | OK |
| `201` | Creado exitosamente |
| `400` | Error de validación en el request |
| `401` | Token inválido, expirado o credenciales incorrectas |
| `403` | Autenticado pero sin permiso sobre el recurso solicitado |
| `404` | Recurso no encontrado |
| `409` | Conflicto de negocio (saldo insuficiente, cuenta ya cerrada, etc.) |
| `500` | Error interno del servidor |
