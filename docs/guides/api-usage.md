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

### 3. Renovar token (cuando el access token expira)

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

## Perfil del cliente

```http
GET /api/v1/profile/{userId}
Authorization: Bearer eyJ...
```

Respuesta:
```json
{
  "fullName": "María Fernanda Atencia",
  "identificationNumber": "1098765432",
  "accountNumber": "5001000002",
  "balance": 200000.0000
}
```

---

## Actualizar datos del cliente

```http
PUT /api/v1/clientes/{id}
Authorization: Bearer eyJ...
Content-Type: application/json

{
  "email": "nuevo@email.com",
  "telefono": "3217654321"
}
```

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
| `404` | Recurso no encontrado |
| `500` | Error interno del servidor |
