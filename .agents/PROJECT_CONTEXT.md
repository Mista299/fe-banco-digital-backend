# 📊 Contexto completo del proyecto

## 🚀 Sprint 1

## HU-27.0 - HU-01 Registro de Nuevos Usuarios

### Estado
Active

### Asignado a
CRISTIAN DAVID ECHEVERRY GONZALEZ <cristian.echeverry1@udea.edu.co>

### Criterios / Descripción
Escenario 1: registro exitoso con datos válidos   Dado que el usuario ingresa su numero de documento (ID) y fecha de expedicion,  cuando el sistema verifica que el id no existe previamente,  entonces habilita el formulario para capturar nombre, correo, direccion, telefono.  y al presionar guardar, el sistema crea el perfil en la base de datos,  y muestra el mensaje: cliente registrado exitosamente, junto al numero de cuenta y saldo.   escenario 2: intento de registro con identificación duplicada  dado que el usuario con identificación 12345 ya está registrado en la base de datos,  cuando un nuevo usuario intenta registrarse con ese mismo número,  entonces el sistema debe impedir el registro,  y mostrar una alerta: el número de identificación ya se encuentra vinculado a una cuenta existente.   escenario 3: validacion de campos obligatorios  dado que el usuario deja campos vacios en el formulario de datos personales o financieros,  cuando intenta presionar el boton de guardar,  entonces el sistema debe impedir el registro en la base de datos,  y emitir un mensaje para los los campos faltantes con el mensaje: &quot;Los campos faltantes son obligatorios para continuar&quot;   

---

## HU-28.0 - HU-03 Consulta de Perfil de Clientes

### Estado
New

### Asignado a
BRYAN DAVID MOLINA DOMINGUEZ <bryan.molina@udea.edu.co>

### Criterios / Descripción
Escenario 1: visualización exitosa del perfil dado que el usuario ya se ha registrado y ha iniciado sesión en la plataforma, cuando accede a la sección de mi perfil o dashboard,  entonces el sistema debe consultar la base de datos usando su identificador único ID,  y mostrar en pantalla su nombre completo y número de identificación,  y listar su cuenta de ahorros activa con su número de 10 dígitos y el saldo disponible.     escenario 2: manejo de error en la consulta de datos Dado que el sistema experimenta una falla de conexión con la base de datos,  Cuando el usuario intenta cargar su perfil,  Entonces el sistema debe mostrar un mensaje de error: no se pudo cargar la información, intente más tarde,  y no debe mostrar campos vacíos o erróneos.    

---

## HU-29.0 - HU-04 Actualizacion del información del Cliente

### Estado
New

### Asignado a
MICHAEL STIVEN TABARES TOBON <michael.tabares@udea.edu.co>

### Criterios / Descripción
escenario 1: actualización exitosa de datos Dado que el usuario se encuentra en la sección de Edición de Perfil,  cuando modifica uno o más campos (teléfono, dirección, correo o fuente de ingresos),  y presiona el botón de guardar cambios,  entonces el sistema debe validar que los nuevos datos tengan el formato correcto,  y debe actualizar el registro en la base de datos,  y mostrar el mensaje: tus datos se han actualizado correctamente.   escenario 2: intento de actualización con datos inválidos dado que el usuario intenta cambiar su correo electrónico,  cuando ingresa un texto que no tiene formato de email (ejemplo: &quot;usuario.com&quot;),  entonces el sistema debe impedir el guardado,  y resaltar el campo en rojo con el mensaje: ingrese un correo electrónico válido.   escenario 3: restricción de campos no editables dado que el usuario está en la pantalla de edición,  cuando visualiza su número de identificación o su número de cuenta,  entonces el sistema debe mostrar estos campos bloqueados (solo lectura),  y no debe permitir que el usuario los modifique por seguridad.   escenario 4: cancelación de cambios dado que el usuario ha escrito nuevos datos en el formulario,  cuando presiona el botón cancelar o regresa al dashboard sin guardar,  entonces el sistema no debe realizar ningún cambio en la base de datos,  y debe mantener la información original del cliente.    

---

## HU-30.0 - HU-05 Cierre del Producto Financiero

### Estado
New

### Asignado a
MARIA FERNANDA ATENCIA OLIVA <mariaf.atencia@udea.edu.co>

### Criterios / Descripción
 criterios de aceptación (gherkin): escenario 1: cierre exitoso de cuenta con saldo cero dado que el usuario tiene una cuenta de ahorros con saldo igual a 0,  cuando selecciona la opción de &quot;cerrar cuenta&quot; y confirma su identidad con su contraseña,  entonces el sistema debe cambiar el estado de la cuenta de &quot;activa&quot; a &quot;Cerrada&quot;,  y debe mostrar en pantalla un mensaje de confirmación al usuario notificando el cierre exitoso.   escenario 2: impedimento de cierre por saldo pendiente dado que el usuario tiene un saldo mayor a 0 en su cuenta (ejemplo: 50.000),  cuando intenta solicitar el cierre de la cuenta,  entonces el sistema debe impedir la acción,  y mostrar el mensaje: no es posible cerrar la cuenta porque aún tienes saldo disponible, por favor retira o traslada tus fondos primero.   escenario 3: visualización de cuenta inactiva en el perfil dado que una cuenta ha sido marcada como &quot;Cerrada&quot;,  cuando el usuario inicia sesión y va a su dashboard,  entonces el sistema no debe permitir realizar transacciones (depósitos o retiros) desde esa cuenta,  y debe mostrar una etiqueta visual que diga: cuenta cerrada.   escenario 4: validación de seguridad para el cierre dado que el usuario solicita el cierre,  cuando el sistema pide la confirmación,  entonces debe validar que el usuario esté autenticado correctamente (re-autenticación),  y si la clave es incorrecta, debe bloquear el proceso de cierre por seguridad.      

---

## HU-33.0 - HU-14: Exposición de movimientos y saldos históricos

### Estado
New

### Asignado a
QUELIX XIOMARA PEREZ ALZATE <xiomara.perez@udea.edu.co>

### Criterios / Descripción
Escenario 1: Visualización del tablero de actividad Dado que el usuario ha realizado transacciones previas (Depósitos, Retiros o Transferencias)  Cuando el usuario accede a la sección de &quot;Movimientos&quot; en su banca digital  Entonces el sistema debe mostrar una lista cronológica con: Fecha/Hora, Concepto, Monto y Saldo Resultante  Y los valores negativos (egresos) deben resaltarse en rojo o con el signo (-) y los positivos (ingresos) en negro o con el signo (+)   Escenario 2: Aplicación de filtros por fecha Dado que el usuario desea localizar una transacción específica de un periodo pasado  Cuando selecciona un rango de fechas (Ej: &quot;01/01/2026&quot; al &quot;15/01/2026&quot;) y aplica el filtro  Entonces la lista debe actualizarse para mostrar únicamente los registros dentro de ese rango    

---

## HU-34.0 - HU-08 Activación de bloqueo preventivo por el usuario

### Estado
New

### Asignado a
BRYAN DAVID MOLINA DOMINGUEZ <bryan.molina@udea.edu.co>

### Criterios / Descripción
Escenario 1: Bloqueo exitoso desde el perfil Dado que el usuario ha detectado una actividad inusual en su cuenta  Cuando selecciona la opción &quot;Bloqueo Preventivo&quot; y confirma la acción con validación de contraseña  Entonces el sistema debe cambiar el estado de la cuenta a &quot;Bloqueada&quot; en el core bancario  Y rechazar cualquier intento de transacción (débito/crédito) hasta que se realice un proceso de desbloqueo seguro.   Escenario 2: Notificación de bloqueo realizado Dado que el sistema ha procesado el bloqueo de la cuenta  Cuando el cambio de estado se confirma exitosamente  Entonces el sistema debe enviar una notificación push&nbsp; al usuario informando la fecha, hora y el canal por el cual se realizó el bloqueo y almacenar el registro    

---


## 🚀 Sprint 2

## HU-26.0 - HU-06 Consulta Detallada de Productos y Movimientos

### Estado
New

### Asignado a
null

### Criterios / Descripción
Escenario 1: Visualización del historial de transacciones Dado que el usuario entra al detalle de su cuenta de ahorros,  Cuando el sistema carga la información,  Entonces debe listar de forma cronológica (del más reciente al más antiguo) los últimos movimientos,  Y cada movimiento debe mostrar: fecha, descripción (ej: &quot;Retiro Cajero&quot;), tipo (Ingreso/Egreso) y el valor de la transacción.   Escenario 2: Diferenciación visual de ingresos y egresos Dado que el usuario visualiza su lista de movimientos,  Cuando el sistema detecta una consignación (entrada), debe mostrar el valor en color verde (o con signo +),  Y cuando detecta un retiro o pago (salida), debe mostrar el valor en color rojo (o con signo -).   Escenario 3: Consulta de cuenta sin movimientos Dado que la cuenta es nueva o no ha tenido actividad en el último mes,  Cuando el usuario accede al detalle,  Entonces el sistema debe mostrar el mensaje: &quot;Aún no registras movimientos en este periodo&quot;.   Escenario 4: Actualización de movimientos en tiempo real Dado que el usuario acaba de realizar una transferencia,  Cuando regresa al detalle de su cuenta,  Entonces el sistema debe refrescar la lista automáticamente para mostrar la transacción más reciente en el primer lugar.    

---

## HU-31.0 - HU-02 Creación Automatica de Cuenta Bancaria

### Estado
New

### Asignado a
null

### Criterios / Descripción
Escenario 1: Generación exitosa de número de cuenta Dado que el perfil del usuario ha sido validado y guardado en la base de datos  Cuando el proceso de registro finalice correctamente  Entonces el sistema debe generar un número de cuenta de ahorros único de 10 dígitos (prefijo 500 para ahorros)  Y el saldo inicial debe mostrarse en $0 COP.   Escenario 2: Notificación de apertura al usuario Dado que la cuenta fue creada con éxito en el núcleo bancario  Cuando el usuario ingresa a su Dashboard por primera vez  Entonces debe visualizar un mensaje de bienvenida con su nuevo número de cuenta y el estado &quot;Activo&quot; y su saldo    

---

## HU-32.0 - HU-07 Visualización de productos y balances disponibles

### Estado
New

### Asignado a
null

### Criterios / Descripción
 Escenario 1: Cliente con múltiples productos activos Dado que el usuario ha iniciado sesión correctamente y tiene más de una cuenta  Cuando el sistema carga el Dashboard principal  Entonces debe listar cada cuenta con su número enmascarado (ej. ****1234), el tipo de producto y el saldo disponible actualizado.   Escenario 2: Cliente con un único producto Dado que el usuario solo posee una cuenta activa en el banco  Cuando accede a su portafolio  Entonces el sistema debe mostrar directamente el detalle de esa cuenta única con su saldo.   Escenario 3: Error de conexión con el servidor de saldos Dado que el servicio de balances del core bancario presenta intermitencia  Cuando el usuario intenta visualizar sus productos  Entonces el sistema debe mostrar la lista de cuentas, pero en el campo de saldo mostrar el mensaje: &quot;Dato no disponible&quot; y ofrecer un botón de &quot;Reintentar carga&quot;.    

---

## HU-35.0 - HU-09 Recarga de saldo mediante canales digitales

### Estado
New

### Asignado a
null

### Criterios / Descripción
Escenario 1: Depósito exitoso con actualización de saldo Dado que el sistema recibe una notificación de abono confirmada por la pasarela de pagos  Cuando el número de cuenta de destino es validado como &quot;Activo&quot; en el Core Bancario  Entonces el sistema debe sumar el monto recibido al saldo disponible de forma inmediata  Y generar un comprobante digital con el número de operación y la fecha exacta.   Escenario 2: Intento de depósito en cuenta bloqueada o inexistente Dado que se intenta realizar un abono a una cuenta con estado &quot;Bloqueada&quot; o &quot;Cerrada&quot; o que no existe  Cuando el motor transaccional procesa la validación de destino  Entonces el sistema debe rechazar la transacción automáticamente  Y devolver los fondos al canal de origen notificando el motivo del rechazo.    

---

## HU-36.0 - HU-10 Autorización de retiro en efectivo mediante token dinámico

### Estado
New

### Asignado a
null

### Criterios / Descripción
 Escenario 1: Generación exitosa de token de retiro Dado que el usuario solicita un retiro por un monto específico  Y el sistema recibe la autorización de viabilidad del Motor de Validación&nbsp;  Cuando el usuario confirma la operación con su clave de seguridad  Entonces el sistema debe generar un código único de 6 dígitos con validez de 30 minutos  Y poner el monto solicitado en estado 'RESERVADO' (restando del saldo disponible pero no del contable) para asegurar que el dinero esté allí cuando el usuario llegue al cajero.   Escenario 2: Interrupción por validación fallida&nbsp; Dado que el usuario intenta generar un código de retiro  Cuando el Motor de Validación&nbsp; retorna &quot;Saldo Insuficiente&quot; o &quot;Cuenta Bloqueada&quot;  Entonces el sistema debe detener la operación inmediatamente  Y mostrar el mensaje de error correspondiente sin generar ningún token ni reserva de fondos.   Escenario 3: Expiración del código y liberación de fondos Dado que han pasado más de 30 minutos y el código no ha sido redimido en un punto físico  Cuando el sistema detecta la expiración del token  Entonces debe anular la validez del código de 6 dígitos  Y liberar automáticamente el monto 'RESERVADO', reintegrándolo al saldo disponible del cliente para que pueda usarlo en otras transacciones.      Escenario4:&nbsp; Persistencia Contable: Dado que la validación de fondos fue exitosa.  Cuando se ejecuta el movimiento de dinero.  Entonces el sistema debe insertar un registro en la tabla Transaccional con: ID_Transacción, Fecha/Hora (ISO 8601), Cuenta_Origen, Cuenta_Destino, Monto y Tipo_Operación.  Y actualizar los saldos de ambas cuentas de forma atómica (Si una falla, la otra no se mueve).     

---

## HU-37.0 - HU-12 Transferencia de fondos a otros bancos nacionales

### Estado
New

### Asignado a
null

### Criterios / Descripción
 Escenario 1: Envío exitoso a otro banco (Proceso ACH) Dado que el usuario ingresa los datos del receptor externo  Y el sistema recibe la autorización del Motor de Validación confirmando saldo y estado de cuenta  Cuando el usuario confirma la operación y autoriza el envío  Entonces el sistema debita el monto de la cuenta origen y pone la transacción en estado &quot;Pendiente de Procesamiento&quot;  Y envía la orden a la red de compensación externa (ACH).   Escenario 2: Rechazo por datos del receptor incorrectos (Post-envío) Dado que la red ACH notifica que los datos del receptor no coinciden  Cuando el sistema recibe el reporte de rechazo del banco destino  Entonces debe realizar una Reversión Automática sumando el valor íntegro al saldo del emisor  Y notificar al usuario el motivo del rechazo para su corrección.   Escenario 3: Rechazo preventivo por Reglas de Negocio Dado que el usuario intenta iniciar una transferencia interbancaria  Cuando el Motor de Validación&nbsp; retorna un código de &quot;Saldo Insuficiente&quot; o &quot;Cuenta Bloqueada&quot;  Entonces el módulo de transferencias debe interrumpir el flujo inmediatamente  Y mostrar el mensaje de alerta correspondiente sin generar ninguna orden hacia la red ACH.      

---

## HU-38.0 - HU-11 Transferencia inmediata a usuarios del mismo banco

### Estado
New

### Asignado a
null

### Criterios / Descripción
 Escenario 1: Ejecución de transferencia interna exitosa Dado que el usuario ingresa un número de cuenta destino válido del mismo banco  Y el sistema recibe la autorización de viabilidad del Motor de Validación  Cuando el emisor confirma la identidad del receptor y autoriza el movimiento  Entonces el sistema debe restar el dinero del emisor y sumarlo al receptor simultáneamente  Y generar una notificación de &quot;Transferencia Exitosa&quot; para ambas partes.   Escenario 2: Interrupción por validación fallida&nbsp; Dado que el usuario intenta realizar una transferencia interna  Cuando el Motor de Validación detecta saldo insuficiente o cuenta bloqueada  Entonces el módulo de transferencias debe detener el proceso de inmediato  Y mostrar el mensaje de error específico retornado por la validación sin afectar los saldos.   Escenario 3: Validación de cuenta destino (Interna) Dado que el usuario ingresa un número de cuenta para transferir  Cuando el sistema busca en la base de datos interna y no encuentra el número de cuenta  Entonces debe notificar: &quot;Cuenta destino no válida o no pertenece a este banco&quot;  Y evitar el llamado al motor de validación de fondos para ahorrar recursos del sistema.   Escenario4:&nbsp; Persistencia Contable: Dado que la validación de fondos fue exitosa.  Cuando se ejecuta el movimiento de dinero.  Entonces el sistema debe insertar un registro en la tabla Transaccional con:&nbsp;ID_Transacción, Fecha/Hora (ISO 8601), Cuenta_Origen, Cuenta_Destino, Monto y Tipo_Operación.  Y actualizar los saldos de ambas cuentas de forma atómica (Si una falla, la otra no se mueve).      

---

## HU-39.0 - HU-13 Motor de Validación

### Estado
New

### Asignado a
null

### Criterios / Descripción
Escenario 1: Validación exitosa de fondos y estado operativo Dado que un cliente inicia una solicitud de retiro o transferencia  Y el sistema consulta el Saldo Disponible y el Estado de la Cuenta  Cuando el saldo es suficiente para cubrir el monto y la cuenta está en estado &quot;ACTIVO&quot;  Entonces el sistema debe autorizar la transacción  Y permitir el paso al módulo de confirmación (OTP/Clave).   Escenario 2: Rechazo por fondos insuficientes (Protección de sobregiro) Dado que el monto de la transacción solicitada es mayor al saldo disponible en la cuenta  Cuando el sistema ejecuta la regla de validación de fondos  Entonces el sistema debe denegar la operación inmediatamente  Y mostrar al cliente el mensaje: &quot;Saldo insuficiente para completar esta operación. &quot;  Y registrar un evento de &quot;Transacción Fallida por Fondos&quot; en el log de auditoría.   Escenario 3: Rechazo por cuenta cerrada o inactiva Dado que el cliente intenta operar desde una cuenta con estado &quot;CERRADA&quot; o &quot;BLOQUEADA&quot;  Cuando el motor de reglas verifica el estado del producto  Entonces el sistema debe bloquear el acceso a cualquier movimiento de salida  Y mostrar el mensaje: &quot;La cuenta de origen no se encuentra habilitada para realizar transacciones&quot;.   Escenario 4: Rechazo por bloqueo de seguridad o embargo Dado que la cuenta tiene fondos pero su estado es &quot;BLOQUEADA&quot; (por reporte de robo o mandato legal)  Cuando se solicita la validación de la transacción  Entonces el sistema debe impedir el débito de los fondos  Y mostrar un mensaje de seguridad: &quot;Operación no permitida. Por razones de seguridad, su cuenta presenta una restricción activa&quot;.    

---


## 🚀 Sprint 3

## HU-40.0 - HU-15 Generación de extracto bancario oficial en formato PDF

### Estado
New

### Asignado a
null

### Criterios / Descripción
Escenario 1: Generación exitosa de extracto de mes cerrado Dado que el usuario se encuentra en la sección de &quot;Documentos y Extractos&quot;  Y selecciona un mes cuyo ciclo contable ya ha finalizado (ejemplo: mes anterior)  Cuando el usuario presiona el botón &quot;Descargar PDF&quot;  Entonces el sistema debe consultar la base de datos de transacciones de ese periodo  Y generar un archivo PDF que incluya:&nbsp; Saldo inicial, Detalle de movimientos, y Saldo final.   Escenario 2: Intento de descarga de mes en curso&nbsp; Dado que el usuario intenta descargar el extracto del mes que aún no ha terminado  Cuando selecciona el mes actual en el menú desplegable  Entonces el sistema debe mostrar un mensaje informativo: &quot;El extracto oficial estará disponible al finalizar el periodo actual&quot;  Y el botón de descarga debe permanecer inhabilitado.   Escenario 3: Integridad y Seguridad del documento Dado que el archivo PDF ha sido generado  Cuando el usuario abre el archivo descargado  Entonces el documento debe tener propiedades de &quot;Solo Lectura&quot; para evitar alteraciones manuales   

---

## HU-41.0 - HU-16 Generacion de Reporte Maestro de Movimientos

### Estado
New

### Asignado a
null

### Criterios / Descripción
Escenario 1: Filtro masivo por rango de fechas Dado que el administrador accede al panel de &quot;Reportes Maestros&quot;.  Cuando ingresa una fecha de inicio y una fecha de fin.  Entonces el sistema debe extraer todas las transacciones (exitosas y fallidas) ocurridas en ese periodo.  Y mostrar el volumen total transaccionado (suma de todos los montos de transacciones exitosas).   Escenario 2: Identificación de cuentas origen y destino Dado que el reporte se ha generado en pantalla.  Entonces cada registro debe mostrar claramente el ID de la cuenta que envió el dinero, el ID de la cuenta que lo recibió y el tipo de canal utilizado (Cajero, Web o App).   Escenario 3: Exportación para análisis externo Dado que el auditor necesita procesar los datos en una herramienta externa (como Excel o Power BI).  Cuando selecciona la opción &quot;Exportar a CSV/Excel&quot;.  Entonces el sistema debe generar un archivo descargable con todas las columnas de la base de datos transaccional.    

---

## HU-42.0 - HU-17 Reporte de Saldos Consolidados

### Estado
New

### Asignado a
null

### Criterios / Descripción
Escenario 1: Cálculo de liquidez total del sistema Dado que el administrador accede al módulo de &quot;Reportes de Saldos&quot;.  Cuando solicita el informe de consolidación global.  Entonces el sistema debe calcular y mostrar la sumatoria total de los saldos de todas las cuentas.  Y desglosar el total por tipo de cuenta (Ahorros vs. Corriente).   Escenario 2: Filtrado por estado de cuenta Dado que el administrador necesita identificar cuentas en riesgo o cerradas  Cuando aplica el filtro por &quot;Estado&quot; (Ej: Bloqueada o cerrada).  Entonces el sistema debe listar todas las cuentas que coincidan con ese estado, mostrando el nombre del titular y el saldo retenido.   Escenario 3: Identificación de cuentas con saldos críticos Dado que se requiere monitorear cuentas con bajo o alto volumen.  Cuando el administrador establece un rango de saldo (Ej: &quot;Cuentas con más de $50,000,000&quot;).  Entonces el sistema debe filtrar el listado para mostrar únicamente los perfiles que cumplen con ese criterio de capital.   Escenario 4: Verificación de saldo actual en tiempo real Dado que el reporte se visualiza en el panel administrativo.  Entonces cada registro debe mostrar el Saldo Disponible y el Saldo Contable (incluyendo canjes o retenciones), junto con el ID único de la cuenta.    

---

## HU-43.0 - HU-18 Búsqueda Avanzada de Actividad por Cliente

### Estado
New

### Asignado a
null

### Criterios / Descripción
Escenario 1: Búsqueda por identificador único Dado que el administrador accede al buscador de &quot;Actividad por Cliente&quot;.  Cuando ingresa el tipo y número de documento (Cédula/NIT) o el número de cuenta.  Entonces el sistema debe validar que el cliente existe en la base de datos y mostrar sus datos básicos (Nombre, Estado de cuenta, Fecha de vinculación).   Escenario 2: Visualización de historial consolidado del cliente Dado que se ha seleccionado un cliente específico.  Cuando el administrador solicita ver la &quot;Actividad Financiera&quot;.  Entonces el sistema debe listar de forma cronológica todos los movimientos (Ingresos, Egresos, Transferencias) asociados exclusivamente a ese usuario.  Y mostrar el saldo acumulado tras cada transacción.   Escenario 3: Filtro de actividad por periodo y tipo Dado que el cliente tiene un historial extenso (más de 100 movimientos).  Cuando el administrador filtra por un rango de fechas o por &quot;Solo Retiros&quot;.  Entonces el sistema debe reducir el listado para mostrar únicamente la actividad que coincide con la búsqueda del funcionario de soporte.   Escenario 4: Auditoría de la consulta. Dado que el administrador está visualizando datos privados de un tercero.  Entonces el sistema debe registrar en el Log de Auditoría Interno: qué administrador consultó a qué cliente y en qué fecha/hora.  Y mantener los datos sensibles (como los últimos dígitos de la tarjeta o cuenta) enmascarados si el rol del administrador no es de nivel &quot;Gerente&quot;.   

---
## El sprint actual es el sprint 1 y se deben priorizar sus HU.

