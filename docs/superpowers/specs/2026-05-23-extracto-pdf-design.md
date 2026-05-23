# Diseño — HU-15: Extracto Bancario Oficial en PDF

**Fecha:** 2026-05-23  
**Rama:** HU-15/Mista  
**Estado:** Aprobado

---

## 1. Contexto

El cliente debe poder descargar un extracto bancario oficial en PDF para cualquier mes cuyo ciclo contable haya cerrado. El mes en curso no está disponible. El PDF debe ser de solo lectura.

---

## 2. Endpoint

```
GET /api/v1/extractos/{idCuenta}/{anio}/{mes}
Authorization: Bearer <token JWT>
Respuesta exitosa: application/pdf (binario)
```

- `idCuenta`: Long — ID de la cuenta a consultar  
- `anio`: int — año (ej. 2026)  
- `mes`: int — mes 1..12 (ej. 1 = enero)

La verificación de propiedad (`idCuenta` pertenece al usuario autenticado) se hace via el token JWT siguiendo el patrón `verificarPropietario` de `TransaccionServiceImpl`.

---

## 3. Casos de salida

| # | Condición | HTTP | Respuesta |
|---|-----------|------|-----------|
| 01 | Mes cerrado, con movimientos | 200 | `application/pdf` |
| 02 | Mes cerrado, sin movimientos | 200 | PDF con saldo igual (inicio = fin) |
| 03 | Mes en curso (año+mes == hoy) | 422 | JSON `{"mensaje": "El extracto oficial estará disponible al finalizar el periodo actual"}` |
| 04 | Cuenta no pertenece al usuario | 403 | JSON error existente (`AccesoNoAutorizadoException`) |
| 05 | Cuenta no encontrada | 404 | JSON error existente (`CuentaNoEncontradaException`) |
| 06 | Sin autenticación (token ausente/inválido) | 401 | JSON error del filtro JWT |
| 07 | Parámetros inválidos (mes < 1, mes > 12, año futuro) | 400 | JSON `{"mensaje": "..."}` |

---

## 4. Arquitectura

### Capas nuevas

```
ExtractoController
    └── ExtractoService (interfaz)
            └── ExtractoServiceImpl
                    ├── UsuarioRepository       — resolver username → Usuario → Cliente
                    ├── CuentaRepository        — verificar propiedad + obtener saldo actual
                    ├── MovimientoRepository    — movimientos en periodo y posteriores
                    ├── TransferenciaRepository — transferencias internas
                    ├── TransferenciaExternaRepository
                    ├── TransferenciaInternacionalRepository
                    ├── TransaccionMapper       — unificar los 4 tipos en MovimientoDTO[]
                    └── ExtractoPdfGenerador    — genera byte[] con PDFBox
```

`ExtractoPdfGenerador` es un `@Component` sin acceso a BD. Recibe un DTO de datos del extracto y devuelve `byte[]`.

### Nueva excepción

`PeriodoNoDisponibleException` → `422 Unprocessable Entity`

---

## 5. Lógica de negocio

### Validación de periodo

```
si anio > ahora.anio  →  400
si anio == ahora.anio && mes > ahora.mes  →  400
si anio == ahora.anio && mes == ahora.mes  →  422 (PeriodoNoDisponibleException)
si mes < 1 || mes > 12  →  400
```

### Rango del periodo

```
inicio = LocalDateTime de (anio, mes, 1, 0, 0, 0)
fin    = LocalDateTime de último día del mes a las 23:59:59.999999999
```

### Cálculo de saldos

Los `MovimientoDTO` ya tienen signo aplicado por `TransaccionMapper`:
- Créditos (depósito, transferencia recibida) → positivo
- Débitos (retiro, transferencia enviada) → negativo

```
movimientos_periodo    = aListaDTOUnificada(todos los tipos en [inicio, fin])
movimientos_posteriores = aListaDTOUnificada(todos los tipos en (fin, ahora])

saldo_final_periodo  = cuenta.getSaldo() - Σ(movimientos_posteriores.monto)
saldo_inicial_periodo = saldo_final_periodo - Σ(movimientos_periodo.monto)
```

### Sumatoria en el PDF

El extracto muestra:
- **Total créditos del periodo** = Σ(movimientos_periodo donde monto > 0)
- **Total débitos del periodo** = Σ(movimientos_periodo donde monto < 0)

---

## 6. Estructura del PDF

```
╔══════════════════════════════════════════╗
║         BANCO DIGITAL                    ║
║    Extracto Bancario Oficial             ║
║    Período: ENERO 2026                   ║
╠══════════════════════════════════════════╣
║ Cuenta:  ****1234  |  AHORROS            ║
║ Titular: Juan Pérez | CC 987654321       ║
║ Generado: 23/05/2026                     ║
╠══════════════════════════════════════════╣
║ SALDO INICIAL:          $1.000.000,00    ║
╠══════════════════════════════════════════╣
║ Fecha     | Descripción  | Monto         ║
║ 01/01/26  | DEPOSITO     | +$200.000     ║
║ 05/01/26  | RETIRO       | -$100.000     ║
║ ...                                      ║
╠══════════════════════════════════════════╣
║ Total créditos:         +$200.000,00     ║
║ Total débitos:          -$100.000,00     ║
║ SALDO FINAL:             $1.100.000,00   ║
╠══════════════════════════════════════════╣
║ Documento oficial - Solo lectura         ║
╚══════════════════════════════════════════╝
```

**Read-only:** `StandardProtectionPolicy` de PDFBox con permisos de solo lectura (sin edición, copia ni impresión modificadora). El archivo se abre sin contraseña.

---

## 7. Dependencia PDFBox

```xml
<dependency>
    <groupId>org.apache.pdfbox</groupId>
    <artifactId>pdfbox</artifactId>
    <version>3.0.3</version>
</dependency>
```

---

## 8. Tests unitarios (JUnit + Mockito)

- `ExtractoControllerTest`: MockMvc sobre los 7 escenarios de salida
- `ExtractoServiceImplTest`: lógica de cálculo de saldos y validación de periodo

## 9. Scripts .sh

Carpeta `scripts/HU-15/` con `_comun.sh` + 7 scripts numerados, siguiendo el patrón de `scripts/HU-09/`.
