Feature: Consulta de Perfil de Clientes (HU-03)

  Background:
    Given un cliente registrado con ID 1 existe en el sistema

  Scenario: Visualización exitosa del perfil
    When el cliente con ID 1 solicita su perfil
    Then la respuesta contiene el nombre completo y número de identificación
    And la respuesta contiene la cuenta de ahorros activa con número de 10 dígitos y saldo

  Scenario: Manejo de error al consultar datos (DB fallida)
    When la base de datos no está disponible
    And el cliente con ID 1 solicita su perfil
    Then el sistema responde con un error amigable: "no se pudo cargar la información, intente más tarde"
