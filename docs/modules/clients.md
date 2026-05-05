# Módulo de Clientes

## ¿Qué hace?

Gestiona la información personal de los clientes del banco y expone el perfil del usuario autenticado.

## Responsabilidades

- Consultar el perfil de un cliente (nombre, documento, cuenta activa, saldo)
- Actualizar campos editables del cliente (teléfono, dirección, correo)
- Proteger campos no editables (documento, número de cuenta)

## Lo que NO hace

- No crea clientes directamente desde la API — el registro de cliente está vinculado al registro de usuario
- No gestiona el estado de la cuenta bancaria (`Cuenta`) — eso es responsabilidad del módulo de cuentas

## Endpoints

| Método | Ruta | Descripción | Auth requerida |
|--------|------|-------------|----------------|
| `GET` | `/api/v1/profile/{userId}` | Consultar perfil del cliente | Sí |
| `PUT` | `/api/v1/clientes/me` | Actualizar datos editables del cliente autenticado | Sí |

> **Origen del ID:** `PUT /api/v1/clientes/me` no recibe ningún ID en la URL ni en el body. El servidor extrae el `username` del JWT y desde ahí resuelve el `idCliente`. No es posible editar los datos de otro cliente desde este endpoint.

## Campos editables vs. de solo lectura

| Campo | Editable |
|-------|----------|
| `nombre` | No |
| `documento` | No |
| `email` | Sí |
| `telefono` | Sí |
| `numeroCuenta` | No |

## Cómo se usa

**Consultar perfil:**
```http
GET /api/v1/profile/1
Authorization: Bearer eyJ...
```

Respuesta:
```json
{
  "fullName": "Bryan Molina",
  "identificationNumber": "1234567890",
  "accountNumber": "5001234567",
  "balance": 150000.0000
}
```

**Actualizar datos:**
```http
PUT /api/v1/clientes/me
Authorization: Bearer eyJ...
Content-Type: application/json

{
  "email": "nuevo@correo.com",
  "telefono": "3001234567"
}
```

> No se envía ningún ID. El servidor identifica al cliente por el token.

## Dependencias

| Módulo / Clase | Para qué |
|----------------|----------|
| `ClienteRepository` | Consultar y actualizar datos del cliente |
| `CuentaRepository` | Obtener la cuenta activa del cliente para el perfil |
| `UsuarioRepository` | Verificar que el usuario autenticado es el propietario del recurso |
| `ClienteMapper` | Convertir `Cliente` → `ProfileDTO` / `ClienteDTO` |
