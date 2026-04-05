# 📋 Metodología de Trabajo — Banco Digital

> **Equipo:** mista, mafe, bryan, xiomi, cristian  
> **Stack:** Java 17, Spring Boot 3, PostgreSQL  
> **Nivel:** Equipo en aprendizaje — mantener todo simple

---

## 1. Estructura de carpetas

Esta es la estructura que usamos. No crear carpetas nuevas sin acordarlo con el equipo.

```
src/main/java/fe/banco_digital/
├── controller/      # Endpoints REST (recibe peticiones, devuelve respuestas)
├── dto/             # Objetos de transferencia de datos (lo que entra y sale de los endpoints)
├── entity/          # Clases que representan las tablas de la base de datos
├── exception/       # Manejo de errores
├── mapper/          # Conversión entre entidades y DTOs
├── repository/      # Consultas a la base de datos
├── service/         # Lógica de negocio
└── web/             # Clase principal de la aplicación (BancoDigitalApplication.java)
```

---

## 2. Reglas de código

### Todo en español
Clases, variables, métodos y nombres de archivos van en **español**.  
Solo se mantienen en inglés palabras técnicas que son estándar: `get`, `set`, `find`, `save`, `id`, `status`, `request`, `response`.

```java
// ✅ Correcto
private String numeroCuenta;
public Cuenta buscarPorId(Long idCuenta) { }

// ❌ Incorrecto
private String accountNumber;
public Account findById(Long id) { }
```

### Nombres de clases por capa

| Carpeta | Sufijo | Ejemplo |
|---|---|---|
| `controller/` | `Controller` | `CuentaController` |
| `service/` | `Service` / `ServiceImpl` | `CuentaService`, `CuentaServiceImpl` |
| `repository/` | `Repository` | `CuentaRepository` |
| `dto/` | `DTO` | `CuentaDTO`, `CrearCuentaDTO` |
| `mapper/` | `Mapper` | `CuentaMapper` |
| `entity/` | sin sufijo | `Cuenta`, `Cliente` |
| `exception/` | `Exception` | `CuentaNoEncontradaException` |

### Entidades — mapeo con la base de datos

Las columnas de la BD están en español. Siempre usar `@Column(name = "...")` aunque el nombre coincida con el campo.

```java
@Entity
@Table(name = "cuenta")
public class Cuenta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cuenta")
    private Long idCuenta;

    @Column(name = "numero_cuenta", unique = true, nullable = false)
    private String numeroCuenta;

    @Column(name = "saldo", precision = 19, scale = 4)
    private BigDecimal saldo;

    @Column(name = "estado", nullable = false)
    @Enumerated(EnumType.STRING)
    private EstadoCuenta estado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_cliente", nullable = false)
    private Cliente cliente;

    // getters y setters
}
```

### Valores monetarios
Siempre usar `BigDecimal` para dinero. Nunca `double` ni `float`.

```java
// ✅ Correcto
private BigDecimal saldo;

// ❌ Incorrecto
private double saldo;
```

---

## 3. Cómo funciona cada capa

### Controller
- Solo recibe la petición y la pasa al service.
- No tiene lógica de negocio.
- Siempre devuelve `ResponseEntity`.

```java
@RestController
@RequestMapping("/api/v1/cuentas")
public class CuentaController {

    private final CuentaService cuentaService;

    public CuentaController(CuentaService cuentaService) {
        this.cuentaService = cuentaService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<CuentaDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(cuentaService.buscarPorId(id));
    }
}
```

### Service
- Aquí va toda la lógica de negocio.
- Siempre crear una **interfaz** y su **implementación** separadas.

```java
// Interfaz
public interface CuentaService {
    CuentaDTO buscarPorId(Long id);
}

// Implementación
@Service
public class CuentaServiceImpl implements CuentaService {

    private final CuentaRepository cuentaRepository;

    public CuentaServiceImpl(CuentaRepository cuentaRepository) {
        this.cuentaRepository = cuentaRepository;
    }

    @Override
    public CuentaDTO buscarPorId(Long id) {
        Cuenta cuenta = cuentaRepository.findById(id)
            .orElseThrow(() -> new CuentaNoEncontradaException(id));
        return new CuentaDTO(cuenta);
    }
}
```

### Repository
- Solo consultas a la base de datos.
- Extender `JpaRepository`.

```java
@Repository
public interface CuentaRepository extends JpaRepository<Cuenta, Long> {
    Optional<Cuenta> findByNumeroCuenta(String numeroCuenta);
}
```

### DTO
- Nunca devolver una entidad directamente desde un endpoint.
- Crear un DTO que tenga solo los campos necesarios.

```java
public class CuentaDTO {
    private Long idCuenta;
    private String numeroCuenta;
    private BigDecimal saldo;
    private String estado;

    public CuentaDTO(Cuenta cuenta) {
        this.idCuenta = cuenta.getIdCuenta();
        this.numeroCuenta = cuenta.getNumeroCuenta();
        this.saldo = cuenta.getSaldo();
        this.estado = cuenta.getEstado().name();
    }

    // getters
}
```

---

## 4. Manejo de errores

Todos los errores se manejan en `GlobalExceptionHandler` dentro de `exception/`.  
No usar try-catch en controllers ni services salvo casos muy específicos.

```java
// 1. Crear la excepción
public class CuentaNoEncontradaException extends RuntimeException {
    public CuentaNoEncontradaException(Long id) {
        super("Cuenta con id " + id + " no encontrada");
    }
}

// 2. Lanzarla en el service
.orElseThrow(() -> new CuentaNoEncontradaException(id));

// 3. El GlobalExceptionHandler la atrapa y devuelve la respuesta al cliente
```

---

## 5. Endpoints y Documentación con Swagger

### Reglas básicas de endpoints
- Siempre bajo `/api/v1/...`
- Usar el nombre del recurso en plural y en español

| Método | URL | Qué hace |
|---|---|---|
| `GET` | `/api/v1/cuentas/{id}` | Obtener una cuenta |
| `GET` | `/api/v1/cuentas` | Listar cuentas |
| `POST` | `/api/v1/cuentas` | Crear cuenta |
| `PUT` | `/api/v1/cuentas/{id}` | Actualizar cuenta |
| `DELETE` | `/api/v1/cuentas/{id}` | Eliminar cuenta |

### Swagger — documentación obligatoria

Todos los endpoints deben estar documentados con las anotaciones de Swagger. No se hace merge de un PR que tenga endpoints sin documentar.

Swagger estará disponible en: `http://localhost:8080/swagger-ui/index.html`

**Dependencia en `pom.xml`:**
```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.3.0</version>
</dependency>
```

**Cómo documentar un controller:**

```java
@RestController
@RequestMapping("/api/v1/cuentas")
@Tag(name = "Cuentas", description = "Operaciones sobre cuentas bancarias")
public class CuentaController {

    @Operation(
        summary = "Buscar cuenta por ID",
        description = "Retorna los datos de una cuenta dado su ID"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Cuenta encontrada"),
        @ApiResponse(responseCode = "404", description = "Cuenta no encontrada")
    })
    @GetMapping("/{id}")
    public ResponseEntity<CuentaDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(cuentaService.buscarPorId(id));
    }

    @Operation(summary = "Crear cuenta", description = "Crea una nueva cuenta bancaria para un cliente")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Cuenta creada exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos")
    })
    @PostMapping
    public ResponseEntity<CuentaDTO> crear(@RequestBody CrearCuentaDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cuentaService.crear(dto));
    }
}
```

**Anotaciones mínimas obligatorias por endpoint:**

| Anotación | Dónde va | Para qué sirve |
|---|---|---|
| `@Tag` | En la clase | Agrupa los endpoints en Swagger |
| `@Operation` | En cada método | Describe qué hace el endpoint |
| `@ApiResponses` | En cada método | Lista los posibles códigos de respuesta |

---

## 6. Git — flujo de trabajo

### Estructura de ramas

```
main        ← producción (no tocar directamente)
└── develop ← rama de integración del equipo
    ├── mista/nombre-del-feature
    ├── mafe/nombre-del-feature
    ├── bryan/nombre-del-feature
    ├── xiomi/nombre-del-feature
    └── cristian/nombre-del-feature
```

### Pasos para trabajar en un feature

```bash
# 1. Ir a develop y actualizarlo
git checkout develop
git pull origin develop

# 2. Crear tu rama
git checkout -b mista/login-usuario

# 3. Trabajar y hacer commits
git add .
git commit -m "feat(usuario): agregar login con JWT"

# 4. Cada 1-2 días traer cambios de develop a tu rama
git checkout develop
git pull origin develop
git checkout mista/login-usuario
git merge develop

# 5. Cuando termines, subir tu rama y abrir PR hacia develop
git push origin mista/login-usuario
```

### Formato de commits

```
feat(modulo):     nueva funcionalidad
fix(modulo):      corrección de bug
refactor(modulo): mejora de código sin cambiar funcionalidad
test(modulo):     agregar o modificar tests
docs(modulo):     cambios en documentación
```

**Ejemplos:**
```
feat(cuenta): agregar endpoint para consultar saldo
fix(transaccion): corregir monto negativo en retiro
refactor(cliente): simplificar validación de documento
```

### Reglas de PR
- El PR va siempre hacia `develop`, nunca directo a `main`.
- Mínimo 1 persona del equipo debe aprobar antes de hacer merge.
- Describir brevemente qué se hizo en el PR.

---

## 7. Checklist antes de hacer PR

- [ ] El código compila sin errores.
- [ ] Probé manualmente que el endpoint funciona.
- [ ] No hay credenciales ni contraseñas en el código.
- [ ] Usé `BigDecimal` para valores de dinero.
- [ ] Los endpoints siguen el patrón `/api/v1/...`.
- [ ] No expuse entidades directamente — usé DTOs.

---

*Ante dudas, consultar con el líder del equipo antes de tomar decisiones que afecten a todos.*
