# FT-01 / HU-01 — Registro de Nuevos Usuarios

## Contexto
Esta entrega cubre la **Feature FT-01 Registro de Nuevos Usuarios**, la **HU-01 Registro de Nuevos Usuarios** y la **Task 45 Lógica de negocio** del equipo **EAV-03 Backend de un Banco Digital**.

## Historia de usuario
Como usuario, quiero registrar mi perfil y datos financieros en la aplicación, para que el sistema cree mi identidad digital.

## Alcance implementado
Se agregó un flujo de registro público que cubre los puntos solicitados en la HU:

1. Validación previa de identidad mediante documento y fecha de expedición.
2. Validación de campos obligatorios.
3. Rechazo de identificación duplicada.
4. Creación del cliente.
5. Creación automática de cuenta bancaria.
6. Creación de identidad digital del usuario.
7. Respuesta con número de cuenta y saldo.

## Endpoints incorporados
### 1) Validar identidad
**POST** `/api/v1/registro/validar-identidad`

#### Request
```json
{
  "documento": "1032456789",
  "fechaExpedicion": "2022-05-10"
}
```

#### Response 200
```json
{
  "disponible": true,
  "mensaje": "Identidad disponible para continuar con el registro."
}
```

### 2) Registrar nuevo usuario
**POST** `/api/v1/registro`

#### Request
```json
{
  "documento": "1032456789",
  "fechaExpedicion": "2022-05-10",
  "nombre": "Cristian Echeverry",
  "email": "cristian@example.com",
  "direccion": "Calle 50 #40-20",
  "telefono": "3001234567",
  "username": "cde571",
  "password": "claveSegura123"
}
```

#### Response 201
```json
{
  "idCliente": 6,
  "idUsuario": 6,
  "idCuenta": 6,
  "numeroCuenta": "48392017",
  "saldo": 0,
  "mensaje": "Cliente registrado exitosamente, junto al número de cuenta y saldo."
}
```

## Reglas de negocio cubiertas
- El documento del cliente debe ser único.
- La fecha de expedición es obligatoria.
- Nombre, email, dirección, teléfono, username y password son obligatorios.
- El correo no puede estar duplicado.
- El username no puede estar duplicado.
- Al finalizar el registro se crea automáticamente una cuenta de ahorros activa con saldo inicial en cero.
- El usuario creado queda con rol `CLIENTE` y estado `ACTIVO`.

## Trazabilidad con los criterios de aceptación
| Criterio | Cobertura |
|---|---|
| Escenario 1: registro exitoso con datos válidos | `POST /api/v1/registro` crea cliente + cuenta + usuario y devuelve cuenta/saldo |
| Escenario 2: identificación duplicada | `IdentificacionDuplicadaException` + respuesta `409 CONFLICT` |
| Escenario 3: campos obligatorios | validaciones `@NotBlank`, `@NotNull`, `@Email`, `@Size` + respuesta `400 BAD_REQUEST` |

## Archivos agregados o actualizados
### Nuevos controladores
- `src/main/java/fe/banco_digital/controller/RegistroUsuarioController.java`

### Nuevos DTOs
- `ValidarIdentidadRequestDTO.java`
- `ValidacionIdentidadResponseDTO.java`
- `RegistroNuevoUsuarioRequestDTO.java`
- `RegistroNuevoUsuarioResponseDTO.java`

### Nuevos servicios
- `RegistroUsuarioService.java`
- `RegistroUsuarioServiceImpl.java`
- `NumeroCuentaService.java`
- `NumeroCuentaServiceImpl.java`

### Nuevas excepciones
- `IdentificacionDuplicadaException.java`
- `CamposObligatoriosException.java`
- `EmailYaExisteException.java`

### Archivos actualizados
- `Cliente.java`
- `ClienteRepository.java`
- `CuentaRepository.java`
- `ConfiguracionSeguridad.java`
- `GlobalExceptionHandler.java`
- `DataLoader.java`

## Consideraciones
- Esta entrega fue preparada para que la HU quede alineada con lo que piden las capturas: documento, fecha de expedición, dirección, creación automática de cuenta y respuesta con saldo.
- No pude ejecutar un build real en este entorno porque el proyecto no trae completo el wrapper de Maven y aquí no está instalado `mvn`, así que la validación fue estática sobre estructura y consistencia del código.
