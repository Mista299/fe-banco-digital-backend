# HU-09 — Depósito vía Pasarela con Autenticación HMAC

## Historia de usuario

Como sistema, quiero recibir notificaciones de depósito de pasarelas de pago externas de forma segura, verificando que cada solicitud proviene de una fuente autorizada mediante firma criptográfica, para evitar que actores no autorizados manipulen los saldos de las cuentas.

## ¿Qué es HMAC y por qué se usa aquí?

HMAC (Hash-based Message Authentication Code) es un mecanismo que permite a dos partes verificar que un mensaje no fue alterado y proviene de quien dice ser, sin exponer la clave secreta.

En este caso la pasarela de pago y el banco comparten un secreto (`GATEWAY_SECRET`). Cuando la pasarela envía una notificación de depósito, calcula `HMAC-SHA256(body, secreto)` y lo adjunta como header. El backend recalcula la firma con el mismo secreto y la compara. Si no coinciden, el request se rechaza con 401 antes de llegar al controlador.

```
Pasarela                              Banco
   │                                     │
   │  POST /api/v1/depositos/notificacion │
   │  X-Gateway-Signature: sha256=<hex>  │
   │  body: { json del depósito }        │
   │ ──────────────────────────────────▶ │
   │                                     │ FiltroHmacGateway
   │                                     │  1. Lee body bytes
   │                                     │  2. HMAC(body, secreto)
   │                                     │  3. Compara con header
   │                                     │  ├── Coincide → continúa
   │                                     │  └── No coincide → 401
```

## Endpoint

### `POST /api/v1/depositos/notificacion`

**Autenticación:** header `X-Gateway-Signature: sha256=<hex>` (no requiere JWT)

**Request**
```json
{
  "numeroCuenta": "00010001",
  "monto": 75000.00,
  "referenciaGateway": "REF-PSE-2026-001",
  "canalOrigen": "PSE"
}
```

**Response 200 — depósito exitoso**
```json
{
  "numeroOperacion": 12,
  "fecha": "2026-04-30T14:33:46.421783",
  "monto": 75000.00,
  "numeroCuentaDestino": "00010001",
  "saldoResultante": 1300000.00,
  "estado": "EXITOSA"
}
```

**Response 401 — firma ausente**
```json
{
  "timestamp": "2026-04-30T14:33:46.421783181",
  "estado": 401,
  "error": "Unauthorized",
  "mensaje": "Firma HMAC ausente. Se requiere el header X-Gateway-Signature."
}
```

**Response 401 — firma inválida**
```json
{
  "timestamp": "2026-04-30T14:33:46.421783181",
  "estado": 401,
  "error": "Unauthorized",
  "mensaje": "Firma HMAC inválida. La notificación no proviene de una pasarela autorizada."
}
```

**Response 422 — depósito rechazado por regla de negocio**
```json
{
  "motivo": "Cuenta bloqueada.",
  "numeroCuenta": "00010001",
  "monto": 75000.00,
  "referenciaGateway": "REF-PSE-2026-001",
  "canalOrigen": "PSE",
  "devolucionSimulada": true,
  "mensajeDevolucion": "Los fondos serán devueltos a la pasarela en un plazo de 24-48 horas hábiles."
}
```

**Response 400 — campos faltantes o monto inválido**
```json
{
  "mensaje": "Error de validación",
  "errores": ["El monto es obligatorio.", "El número de cuenta destino es obligatorio."]
}
```

## Implementación técnica

### 1. Filtro HMAC — `FiltroHmacGateway`

Extiende `OncePerRequestFilter`, lo que garantiza que se ejecuta exactamente una vez por request, antes del controlador.

**`shouldNotFilter`**

```java
protected boolean shouldNotFilter(HttpServletRequest request) {
    return !request.getRequestURI().startsWith("/api/v1/depositos/");
}
```

El filtro solo actúa sobre rutas `/api/v1/depositos/**`. El resto del sistema (login, consultas, transacciones) no pasa por él.

**Problema del body en Servlet**

El `InputStream` de un `HttpServletRequest` solo se puede leer una vez. Si el filtro lo lee para calcular la firma, el controlador recibe un stream vacío y no puede deserializar el JSON.

La solución es `SolicitudConCuerpoEnCache`, una clase interna que extiende `HttpServletRequestWrapper`:

```
Request original
  └── SolicitudConCuerpoEnCache (wrapper)
        ├── Lee body completo en byte[] al construirse
        ├── getCuerpo()       → devuelve los bytes al filtro para HMAC
        └── getInputStream()  → devuelve nuevo ByteArrayInputStream con los mismos bytes
            getReader()       → igual, para lectura como texto
```

Esto permite que tanto el filtro como Spring MVC (y por tanto Jackson) lean el cuerpo independientemente.

**Cálculo de la firma**

```java
Mac mac = Mac.getInstance("HmacSHA256");
mac.init(new SecretKeySpec(secretoBytes, "HmacSHA256"));
return "sha256=" + HexFormat.of().formatHex(mac.doFinal(cuerpo));
```

- `cuerpo` son los bytes crudos del body tal como llegaron por la red.
- El secreto se convierte a bytes UTF-8 una sola vez en el constructor del filtro.
- El resultado tiene siempre el prefijo `sha256=` seguido de 64 caracteres hexadecimales.

**Comparación segura contra timing attacks**

```java
MessageDigest.isEqual(firmaEsperada.getBytes(UTF_8), firmaRecibida.getBytes(UTF_8))
```

`MessageDigest.isEqual` compara en tiempo constante (no cortocircuita al primer byte diferente). Esto evita que un atacante deduzca cuántos caracteres de la firma son correctos midiendo el tiempo de respuesta.

**Configuración en Spring Security**

```java
.addFilterBefore(filtroHmacGateway(), UsernamePasswordAuthenticationFilter.class)
```

El filtro HMAC se ejecuta antes del filtro JWT, por lo que las rutas de depósito se validan por firma antes de cualquier intento de autenticación por token.

La ruta `/api/v1/depositos/**` está marcada como `permitAll()` en `ConfiguracionSeguridad` porque la autenticación la gestiona el propio `FiltroHmacGateway`, no Spring Security.

### 2. Cálculo de la firma del lado del cliente

La pasarela debe calcular el HMAC sobre el body exacto que enviará (mismo encoding, sin espacios adicionales):

```bash
BODY='{"numeroCuenta":"00010001","monto":75000.0,...}'
FIRMA=$(echo -n "$BODY" | openssl dgst -sha256 -hmac "$GATEWAY_SECRET" | awk '{print "sha256="$2}')
```

> El `-n` en `echo` es crítico: sin él se añade un `\n` al body y la firma no coincide con la que calcula el servidor.

### 3. Servicio de depósito — `DepositoGatewayServiceImpl`

Una vez superado el filtro HMAC, el servicio aplica las reglas de negocio:

```
procesarNotificacion(notificacion)
  ├── findByNumeroCuentaConLock(numeroCuenta)
  │     └── Si null → DepositoRechazadoException("Cuenta no encontrada.")
  ├── cuenta.getEstado() == BLOQUEADA
  │     └── registrarFallo + DepositoRechazadoException("Cuenta bloqueada.")
  ├── cuenta.getEstado() == INACTIVA
  │     └── registrarFallo + DepositoRechazadoException("Cuenta cerrada.")
  ├── cuenta.setSaldo(saldo + monto)          → actualiza saldo
  ├── cuentaRepository.save(cuenta)
  ├── new Transaccion(DEPOSITO, EXITOSA, ...)
  └── retorna ComprobanteDepositoDTO
```

**Lock pesimista**

```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
Optional<Cuenta> findByNumeroCuentaConLock(String numeroCuenta)
```

Se usa lock pesimista para evitar condiciones de carrera: si dos notificaciones llegan simultáneamente para la misma cuenta, la segunda espera hasta que la primera termine y actualice el saldo. Sin esto, ambas leerían el mismo saldo y el segundo depósito sobrescribiría el primero.

**`@Transactional`**

El método `procesarNotificacion` está anotado con `@Transactional`. Si falla en cualquier punto después de actualizar el saldo (por ejemplo, al guardar la transacción), toda la operación se revierte. No puede quedar un saldo actualizado sin su registro de transacción.

### 4. Variable de entorno

| Variable | Descripción |
|----------|-------------|
| `GATEWAY_SECRET` | Clave compartida con la pasarela. Mínimo 32 caracteres. Nunca en código fuente. |

En `application.properties`:
```properties
app.gateway.secreto=${GATEWAY_SECRET}
```

En `.env` (local, gitignored):
```
GATEWAY_SECRET=clave_secreta_pasarela_banco_2026_hmac
```

## Archivos involucrados

| Archivo | Rol |
|---------|-----|
| `security/FiltroHmacGateway.java` | Filtro que valida la firma HMAC antes de pasar al controlador |
| `controller/DepositoGatewayController.java` | Recibe la notificación y delega al servicio |
| `service/DepositoGatewayService.java` | Interfaz del servicio |
| `service/DepositoGatewayServiceImpl.java` | Lógica de negocio: validación de cuenta + actualización de saldo |
| `dto/NotificacionDepositoDTO.java` | Body del request con validaciones `@NotBlank`, `@NotNull`, `@DecimalMin` |
| `dto/ComprobanteDepositoDTO.java` | Response en caso de éxito |
| `dto/RechazoDepositoDTO.java` | Response en caso de rechazo de negocio (422) |
| `exception/DepositoRechazadoException.java` | Excepción que lleva el DTO de rechazo al `GlobalExceptionHandler` |
| `security/ConfiguracionSeguridad.java` | Registra el filtro y declara `/api/v1/depositos/**` como `permitAll` |
| `test/controller/DepositoGatewayControllerTest.java` | Tests unitarios del controlador con firma real |
| `scripts/HU-09/` | Scripts de prueba manual con `openssl` |
