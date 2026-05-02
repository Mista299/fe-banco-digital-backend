# HU-07 — Dashboard de Cuentas

## Historia de usuario

Como cliente del banco digital, quiero ver la lista de todas mis cuentas con sus respectivos saldos actuales en la pantalla de inicio, para conocer mi situación financiera de un vistazo y seleccionar rápidamente el producto que deseo gestionar.

## Alcance implementado

- Endpoint autenticado que retorna todas las cuentas del cliente activo.
- Número de cuenta enmascarado (`****XXXX`) para proteger la información en pantalla.
- Indicadores visuales por estado: cuentas cerradas se marcan con `etiquetaVisual` y tienen `permiteTransacciones: false`.
- Campo `saldoDisponible` que el frontend usa para mostrar "Dato no disponible" cuando el servicio no responde.

## Endpoint

### `GET /api/v1/cuentas/dashboard`

**Autenticación:** cookie `accessToken` (JWT)

**Response 200**
```json
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

**Response 401** — sin sesión activa
```json
{ "mensaje": "No autenticado. Incluye un token Bearer válido." }
```

## Campos del DTO

| Campo | Tipo | Descripción |
|-------|------|-------------|
| `idCuenta` | `Long` | ID interno de la cuenta |
| `numeroCuenta` | `String` | Número completo (solo para operaciones internas) |
| `numeroEnmascarado` | `String` | Últimos 4 dígitos visibles: `****1234` |
| `tipo` | `String` | `AHORROS` o `CORRIENTE` |
| `saldo` | `BigDecimal` | Saldo actual. `null` si `saldoDisponible` es `false` |
| `saldoDisponible` | `boolean` | `true` en operación normal. `false` si el servicio de saldos falla |
| `estado` | `String` | `ACTIVA`, `INACTIVA` o `BLOQUEADA` |
| `permiteTransacciones` | `boolean` | `true` solo si `estado == ACTIVA` |
| `etiquetaVisual` | `String` | `"Cuenta Cerrada"` si `INACTIVA`, `null` en otro caso |

## Trazabilidad con los escenarios

| Escenario | Comportamiento del backend |
|-----------|---------------------------|
| Escenario 1: múltiples productos activos | Retorna todos los registros con `numeroEnmascarado`, `tipo` y `saldo` |
| Escenario 2: único producto | Retorna lista de un elemento — el frontend detecta `length == 1` y muestra el detalle |
| Escenario 3: error de conexión | El endpoint retorna 5xx o no responde — el frontend usa `saldoDisponible: false` como señal para mostrar "Dato no disponible" |

## Flujo interno

```
GET /api/v1/cuentas/dashboard
  └── FiltroJwt                        → extrae username del token
  └── CuentaController.obtenerDashboard
  └── CuentaServiceImpl.obtenerCuentasDelCliente(username)
        ├── UsuarioRepository.findByUsername        → obtiene id del cliente
        ├── CuentaRepository.findByCliente_IdCliente → lista todas las cuentas
        └── CuentaMapper.aCuentaResumenDTO           → convierte a DTO con enmascarado
```

## Archivos involucrados

| Archivo | Cambio |
|---------|--------|
| `dto/CuentaResumenDTO.java` | Campos nuevos: `numeroEnmascarado`, `saldoDisponible`. Lógica de enmascarado en constructor |
| `controller/CuentaController.java` | Endpoint `GET /dashboard` existente, sin cambios |
| `service/CuentaServiceImpl.java` | Método `obtenerCuentasDelCliente`, sin cambios |
| `test/controller/CuentaControllerTest.java` | Tests nuevos para los 3 escenarios |
| `test/service/CuentaServiceImplTest.java` | Tests nuevos del servicio |
| `scripts/HU-07-dashboard/` | Scripts de prueba manuales |
