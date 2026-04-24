Feature: FT-01 - Registro de nuevos usuarios
  HU-01: Como usuario, quiero registrar mi perfil y datos financieros en la aplicación,
  para que el sistema cree mi identidad digital y mi primera cuenta de ahorros.

  Background:
    Given que el sistema de registro está disponible
    And la base de datos está operativa

  # ─────────────────────────────────────────────
  # HU-01 · Registro principal
  # ─────────────────────────────────────────────

  Scenario: HU-01-E01 - Registro exitoso con datos válidos
    Given que el usuario ingresa el documento "1234567890" con fecha de expedición "2010-05-20"
    When el sistema verifica que el documento no existe previamente
    Then el sistema habilita el formulario de datos personales
    When el usuario completa los siguientes campos:
      | campo              | valor                     |
      | nombre             | Juan Pérez                |
      | correo             | juan.perez@email.com      |
      | dirección          | Calle 10 # 5-30           |
      | teléfono           | 3001234567                |
    And presiona el botón "Guardar"
    Then el sistema crea el perfil en la base de datos
    And genera un número de cuenta de 10 dígitos con prefijo "500"
    And el saldo inicial de la cuenta es "$0 COP"
    And muestra el mensaje "Cliente registrado exitosamente"
    And muestra el número de cuenta generado

  Scenario Outline: HU-01-E02 - Intento de registro con identificación duplicada
    Given que el usuario con documento "<id_existente>" ya está registrado en la base de datos
    When un nuevo usuario intenta registrarse con el documento "<id_existente>"
    Then el sistema impide el registro
    And muestra el mensaje "El número de identificación ya se encuentra vinculado a una cuenta existente"

    Examples:
      | id_existente |
      | 12345        |
      | 98765432     |
      | 1000000001   |

  Scenario: HU-01-E03 - Validación de campos obligatorios vacíos
    Given que el usuario está en el formulario de datos personales
    When deja los siguientes campos vacíos:
      | campo     |
      | nombre    |
      | correo    |
      | teléfono  |
    And presiona el botón "Guardar"
    Then el sistema impide el registro en la base de datos
    And resalta en rojo cada campo vacío
    And muestra el mensaje "Los campos faltantes son obligatorios para continuar"

  # ─────────────────────────────────────────────
  # HU-01-B · Validaciones de formato en registro
  # ─────────────────────────────────────────────

  Scenario: HU-01B-E01 - Correo con formato inválido en el registro
    Given que el usuario está en el formulario de datos personales
    When ingresa el correo "usuariosindominio.com"
    Then el sistema resalta el campo correo en rojo
    And muestra el mensaje "Ingrese un correo electrónico válido"
    And el botón "Guardar" permanece deshabilitado

  Scenario: HU-01B-E02 - Teléfono con caracteres no numéricos
    Given que el usuario está en el formulario de datos personales
    When ingresa el teléfono "abc123xyz"
    Then el sistema resalta el campo teléfono en rojo
    And muestra el mensaje "El teléfono solo debe contener números"
    And el botón "Guardar" permanece deshabilitado

  Scenario Outline: HU-01B-E03 - Documento con longitud inválida
    Given que el usuario está en el paso de verificación de documento
    When ingresa el número de documento "<documento>"
    Then el sistema muestra el mensaje "<mensaje_error>"
    And no permite continuar al formulario de datos personales

    Examples:
      | documento   | mensaje_error                                          |
      | 12          | El número de documento debe tener entre 6 y 10 dígitos |
      | 12345678901 | El número de documento debe tener entre 6 y 10 dígitos |
