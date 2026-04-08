# Banco Digital

Backend REST de un sistema de banca digital que permite gestionar clientes, cuentas y transacciones con autenticacion basada en JWT.

**Equipo:** mista · mafe · bryan · xiomi · cristian  
**Documentacion completa:** [docs/index.md](docs/index.md)

---

## Estructura del proyecto

```
banco-digital/
├── src/
│   ├── main/
│   │   ├── java/fe/banco_digital/
│   │   │   ├── controller/     # Endpoints REST
│   │   │   ├── dto/            # Objetos de entrada y salida de los endpoints
│   │   │   ├── entity/         # Clases que representan las tablas de la base de datos
│   │   │   ├── exception/      # Excepciones personalizadas y manejo global de errores
│   │   │   ├── mapper/         # Conversion entre entidades y DTOs
│   │   │   ├── repository/     # Consultas a la base de datos
│   │   │   ├── security/       # Configuracion JWT y filtros de seguridad
│   │   │   ├── service/        # Logica de negocio (interfaz + implementacion)
│   │   │   └── web/            # Clase principal de la aplicacion
│   │   └── resources/
│   │       └── application.properties   # Configuracion de Spring (DB, JPA, JWT)
│   └── test/                   # Tests de integracion
│
├── docs/                       # Documentacion del proyecto
│   ├── guides/                 # Guias practicas (inicio rapido, API, flujo Git)
│   ├── modules/                # Descripcion de cada modulo de negocio
│   ├── decisions/              # Decisiones de arquitectura (ADRs)
│   ├── diagrams/               # Diagramas de arquitectura, base de datos y autenticacion
│   └── arqui/                  # Entregables formales del sprint
│
├── .agents/                    # Contexto e instrucciones para Claude Code
├── scripts/                    # Scripts de utilidad
├── pom.xml                     # Dependencias y configuracion de Maven
└── scripts/run.sh              # Script para levantar la aplicacion
```
