# Datos de prueba (seed)

## Cómo activar

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=seed
```

El `DataLoader` solo se ejecuta bajo el perfil `seed`. Es **idempotente** — usa `findBy…OrElseGet` antes de insertar, por lo que ejecutarlo varias veces no duplica datos.

## Qué crea

### Roles
| Nombre |
|--------|
| `ADMIN` |
| `CLIENTE` |

### Clientes y usuarios

| Cliente | Username | Cuenta | Saldo |
|---------|----------|--------|-------|
| Bryan (cliente 1) | `bryan` | `5001000001` | $500.000 |
| Ana (cliente 2) | `ana` | `5001000002` | $200.000 |

### Cuentas adicionales
| Número | Tipo | Titular | Saldo |
|--------|------|---------|-------|
| `5001000003` | CORRIENTE | Bryan | $0 |

### Transacciones de ejemplo
| Tipo | Origen | Destino | Monto |
|------|--------|---------|-------|
| DEPOSITO | — | `5001000001` | $500.000 |
| DEPOSITO | — | `5001000002` | $200.000 |
| TRANSFERENCIA | `5001000001` | `5001000002` | $50.000 |

### Auditoría
Dos registros de ejemplo con las acciones `LOGIN` y `TRANSFERENCIA`.

## Credenciales para probar

```http
POST /api/v1/auth/login
{ "username": "bryan", "password": "1234" }
```
