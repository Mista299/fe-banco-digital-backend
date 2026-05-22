# Comandos rápidos - Sprint 3

## Docker

```powershell
copy .env.example .env
docker build -t banco-digital-backend:latest .
docker compose up --build
```

## Validar backend local

```powershell
curl http://localhost:8080/actuator/health
curl http://localhost:8080/actuator/prometheus
```

## Kubernetes con Docker Desktop

```powershell
kubectl apply -f k8s/
kubectl get pods -n banco-digital
kubectl get svc -n banco-digital
kubectl logs deployment/banco-digital-backend -n banco-digital
```

## Kubernetes con Minikube

```powershell
minikube start
minikube image build -t banco-digital-backend:latest .
kubectl apply -f k8s/
minikube service banco-digital-backend-nodeport -n banco-digital
minikube service prometheus -n banco-digital
minikube service grafana -n banco-digital
```

## Eliminar despliegue

```powershell
kubectl delete namespace banco-digital
```
