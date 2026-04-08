# Casos de prueba — HU-01 Registro de Nuevos Usuarios

## CP-01 Registro exitoso
**Dado** un usuario con documento no existente y campos completos.
**Cuando** envía la solicitud de registro.
**Entonces** el sistema crea cliente, cuenta y usuario, y devuelve número de cuenta y saldo.

## CP-02 Validación previa de identidad
**Dado** un documento no existente.
**Cuando** el usuario llama el endpoint de validación de identidad.
**Entonces** el sistema responde que la identidad está disponible.

## CP-03 Documento duplicado
**Dado** un documento que ya existe en la base de datos.
**Cuando** el usuario intenta validar o registrar con ese documento.
**Entonces** el sistema responde `409 CONFLICT` indicando que la identificación ya está vinculada.

## CP-04 Campos obligatorios vacíos
**Dado** una solicitud con nombre, dirección, email o teléfono vacíos.
**Cuando** el usuario envía el formulario.
**Entonces** el sistema responde `400 BAD_REQUEST` con el detalle de los campos faltantes.

## CP-05 Correo duplicado
**Dado** un correo ya registrado.
**Cuando** el usuario intenta registrarse con ese correo.
**Entonces** el sistema responde `409 CONFLICT`.

## CP-06 Username duplicado
**Dado** un nombre de usuario ya existente.
**Cuando** el usuario intenta registrarse con ese username.
**Entonces** el sistema responde `409 CONFLICT`.
