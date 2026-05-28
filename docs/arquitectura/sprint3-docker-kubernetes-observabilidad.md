# Sprint 3 - Entregable de Arquitectura: Docker, Kubernetes y Observabilidad

Responsable: Cristian David Echeverry González  
Proyecto: EAV-03 Backend de un Banco Digital  
Alcance: puntos 2 y 3 del entregable de arquitectura.

## Checklist del entregable

| Punto solicitado | Estado | Evidencia incluida |
|---|---:|---|
| 2. Realizar contenedores Docker | ✅ | `Dockerfile`, `docker-compose.yml`, `.env.example` |
| 2. Desplegar en un orquestador Kubernetes | ✅ | Carpeta `k8s/` con namespace, configmap, secret, deployments y services |
| 3. Integración con Grafana-Prometheus | ✅ | `monitoring/prometheus/prometheus.yml`, configuración Grafana y manifiestos `k8s/06-prometheus.yaml`, `k8s/07-grafana.yaml` |
| Métricas del backend | ✅ | Spring Boot Actuator + Micrometer Prometheus |
| Endpoints de observabilidad | ✅ | `/actuator/health`, `/actuator/prometheus`, `/actuator/metrics` |
| Seguridad mínima de secretos | ✅ | Variables por `Secret` en Kubernetes y `.env.example` sin claves reales |

---

## 1. Contenerización con Docker

Se definió un `Dockerfile` multi-stage. La primera etapa usa Maven para compilar el proyecto y generar el archivo `.jar`; la segunda etapa usa una imagen JRE más liviana para ejecutar la aplicación.

Comando de construcción:

```powershell
docker build -t banco-digital-backend:latest .
```

Comando de ejecución local:

```powershell
docker run --name banco-digital-backend --env-file .env -p 8080:8080 banco-digital-backend:latest
```

También se agregó `docker-compose.yml` para levantar:

- backend Spring Boot
- PostgreSQL
- Prometheus
- Grafana

Comando:

```powershell
docker compose up --build
```

Validaciones:

```text
http://localhost:8080/actuator/health
http://localhost:8080/actuator/prometheus
http://localhost:9090
http://localhost:3001
```

Credenciales de Grafana local:

```text
usuario: admin
contraseña: admin
```

---

## 2. Despliegue en Kubernetes

Se agregó la carpeta `k8s/` con los manifiestos necesarios para desplegar el backend y sus dependencias.

Archivos incluidos:

```text
k8s/00-namespace.yaml
k8s/01-configmap.yaml
k8s/02-secret.yaml
k8s/03-postgres.yaml
k8s/04-backend.yaml
k8s/05-backend-nodeport.yaml
k8s/06-prometheus.yaml
k8s/07-grafana.yaml
```

Aplicar manifiestos:

```powershell
kubectl apply -f k8s/
```

Verificar recursos:

```powershell
kubectl get all -n banco-digital
kubectl get pods -n banco-digital
kubectl get svc -n banco-digital
```

Ver logs del backend:

```powershell
kubectl logs deployment/banco-digital-backend -n banco-digital
```

Acceso usando NodePort:

```text
Backend:    http://localhost:30080/actuator/health
Prometheus: http://localhost:30090
Grafana:    http://localhost:30030
```

En Minikube también se puede usar:

```powershell
minikube service banco-digital-backend-nodeport -n banco-digital
minikube service prometheus -n banco-digital
minikube service grafana -n banco-digital
```

---

## 3. Observabilidad con Prometheus y Grafana

Se integró Spring Boot Actuator y Micrometer Prometheus para que el backend exponga métricas técnicas.

Dependencias agregadas al `pom.xml`:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-registry-prometheus</artifactId>
</dependency>
```

Configuración agregada en `application.properties`:

```properties
management.endpoints.web.exposure.include=health,info,metrics,prometheus
management.endpoint.health.show-details=always
management.prometheus.metrics.export.enabled=true
management.metrics.tags.application=${spring.application.name}
```

Prometheus consulta el backend en:

```text
/actuator/prometheus
```

La configuración está en:

```text
monitoring/prometheus/prometheus.yml
k8s/06-prometheus.yaml
```

Grafana queda conectado a Prometheus como datasource. Se incluye provisioning para facilitar la evidencia del dashboard.

---

## Evidencias recomendadas para anexar al informe

1. Captura de `docker build -t banco-digital-backend:latest .`
2. Captura de `docker compose up --build`
3. Captura de `/actuator/health` respondiendo `UP`
4. Captura de `/actuator/prometheus` mostrando métricas
5. Captura de `kubectl get pods -n banco-digital`
6. Captura de `kubectl get svc -n banco-digital`
7. Captura de Prometheus con target `banco-digital-backend` en estado `UP`
8. Captura de Grafana con dashboard de métricas

---

## Conclusión técnica

Con esta configuración, el backend queda preparado para ejecutarse en contenedores Docker, desplegarse en Kubernetes y exponer métricas de operación para ser recolectadas por Prometheus y visualizadas en Grafana. Esto cumple los puntos 2 y 3 del entregable de arquitectura del Sprint 3.
