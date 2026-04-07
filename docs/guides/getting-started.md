# Cómo ejecutar el proyecto

## Requisitos

- Java 17+
- Maven 3.8+ (o usar el wrapper `./mvnw` incluido)
- PostgreSQL 14+ con una base de datos llamada `banco2026`

## Configuración

Copia el archivo de ejemplo y ajusta los valores:

```bash
cp application-local.properties.example src/main/resources/application-local.properties
```

O crea un archivo `.env` en la raíz del proyecto (recomendado):

```env
DB_URL=jdbc:postgresql://localhost:5432/banco2026
DB_USER=postgres
DB_PASS=
DDL_AUTO=update
JWT_SECRET=clave-secreta-larga-minimo-32-caracteres
```

> `DDL_AUTO=update` conserva el esquema entre reinicios. En un entorno limpio, el valor por defecto es `create` (borra y recrea todo).

## Ejecutar

```bash
# Con carga automática del .env
./run.sh

# Sin el script
./mvnw spring-boot:run

# Con datos de prueba
./mvnw spring-boot:run -Dspring-boot.run.profiles=seed
```

## Tests

Los tests usan H2 en memoria — no requieren PostgreSQL:

```bash
# Todos los tests
./mvnw test

# Un test específico
./mvnw test -Dtest=NombreDeLaClaseTest
```

## Build

```bash
./mvnw package -DskipTests
# El .jar queda en target/
```

## Verificar que funciona

```bash
curl http://localhost:8080/api/db-ping
# → {"status":"ok"}
```

## Swagger UI

Con la aplicación corriendo, abre:

```
http://localhost:8080/swagger-ui.html
```
