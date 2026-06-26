# SathishLogger Deployment Guide

This guide covers different deployment scenarios for SathishLogger across various environments.

## 🚀 Quick Deployment

### Option 1: One-Command Deployment
```bash
# Build and run everything
./build-and-run.sh

# Or step by step
./build-and-run.sh build
./build-and-run.sh run
./build-and-run.sh test
```

### Option 2: Manual Docker Compose
```bash
# Setup environment
cp .env.example .env
# Edit .env as needed

# Start services
docker-compose up -d

# Check status
docker-compose ps
curl http://localhost:8080/api/logs/health
```

## 🏢 Production Deployments

### AWS ECS with Application Load Balancer

1. **Create Task Definition**
```json
{
  "family": "sathishlogger",
  "networkMode": "awsvpc",
  "requiresCompatibilities": ["FARGATE"],
  "cpu": "512",
  "memory": "1024",
  "executionRoleArn": "arn:aws:iam::account:role/ecsTaskExecutionRole",
  "containerDefinitions": [
    {
      "name": "sathishlogger",
      "image": "your-registry/sathishlogger:latest",
      "portMappings": [{"containerPort": 8080}],
      "environment": [
        {"name": "DATABASE_URL", "value": "jdbc:postgresql://rds-endpoint:5432/sathishlogger"},
        {"name": "DATABASE_USERNAME", "value": "sathish"},
        {"name": "DATABASE_PASSWORD", "value": "secure-password"},
        {"name": "LOG_LEVEL", "value": "WARN"}
      ],
      "logConfiguration": {
        "logDriver": "awslogs",
        "options": {
          "awslogs-group": "/ecs/sathishlogger",
          "awslogs-region": "us-east-1",
          "awslogs-stream-prefix": "ecs"
        }
      }
    }
  ]
}
```

2. **Create ECS Service**
```bash
aws ecs create-service \
  --cluster production \
  --service-name sathishlogger \
  --task-definition sathishlogger:1 \
  --desired-count 2 \
  --launch-type FARGATE \
  --network-configuration "awsvpcConfiguration={subnets=[subnet-12345],securityGroups=[sg-12345],assignPublicIp=ENABLED}" \
  --load-balancers "targetGroupArn=arn:aws:elasticloadbalancing:region:account:targetgroup/sathishlogger/1234567890123456,containerName=sathishlogger,containerPort=8080"
```

### Google Cloud Run

1. **Build and Push Image**
```bash
# Build for Cloud Run
docker build -t gcr.io/PROJECT-ID/sathishlogger .
docker push gcr.io/PROJECT-ID/sathishlogger
```

2. **Deploy to Cloud Run**
```bash
gcloud run deploy sathishlogger \
  --image gcr.io/PROJECT-ID/sathishlogger \
  --platform managed \
  --region us-central1 \
  --allow-unauthenticated \
  --set-env-vars="DATABASE_URL=jdbc:postgresql://CLOUD-SQL-IP:5432/sathishlogger,DATABASE_USERNAME=sathish,DATABASE_PASSWORD=secure-password" \
  --memory 1Gi \
  --cpu 1 \
  --max-instances 10
```

### Azure Container Instances

```bash
az container create \
  --resource-group myResourceGroup \
  --name sathishlogger \
  --image your-registry/sathishlogger:latest \
  --cpu 1 \
  --memory 2 \
  --ports 8080 \
  --environment-variables \
    DATABASE_URL=jdbc:postgresql://azure-postgres:5432/sathishlogger \
    DATABASE_USERNAME=sathish \
    DATABASE_PASSWORD=secure-password \
    LOG_LEVEL=INFO
```

## ☸️ Kubernetes Deployments

### Basic Kubernetes Deployment

```yaml
# k8s-deployment.yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: sathishlogger-config
data:
  DATABASE_URL: "jdbc:postgresql://postgres-service:5432/sathishlogger"
  LOG_LEVEL: "INFO"
  STORAGE_TYPE: "database"
---
apiVersion: v1
kind: Secret
metadata:
  name: sathishlogger-secrets
type: Opaque
stringData:
  DATABASE_USERNAME: "sathish"
  DATABASE_PASSWORD: "secure-password"
---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: sathishlogger
  labels:
    app: sathishlogger
spec:
  replicas: 3
  selector:
    matchLabels:
      app: sathishlogger
  template:
    metadata:
      labels:
        app: sathishlogger
    spec:
      containers:
      - name: sathishlogger
        image: sathishlogger:latest
        ports:
        - containerPort: 8080
        envFrom:
        - configMapRef:
            name: sathishlogger-config
        - secretRef:
            name: sathishlogger-secrets
        livenessProbe:
          httpGet:
            path: /api/logs/health
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 10
        readinessProbe:
          httpGet:
            path: /api/logs/health
            port: 8080
          initialDelaySeconds: 5
          periodSeconds: 5
        resources:
          requests:
            memory: "512Mi"
            cpu: "250m"
          limits:
            memory: "1Gi"
            cpu: "500m"
---
apiVersion: v1
kind: Service
metadata:
  name: sathishlogger-service
spec:
  selector:
    app: sathishlogger
  ports:
  - port: 80
    targetPort: 8080
  type: LoadBalancer
---
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: sathishlogger-ingress
  annotations:
    kubernetes.io/ingress.class: "nginx"
    cert-manager.io/cluster-issuer: "letsencrypt-prod"
spec:
  tls:
  - hosts:
    - sathishlogger.yourdomain.com
    secretName: sathishlogger-tls
  rules:
  - host: sathishlogger.yourdomain.com
    http:
      paths:
      - path: /
        pathType: Prefix
        backend:
          service:
            name: sathishlogger-service
            port:
              number: 80
```

### Deploy to Kubernetes
```bash
kubectl apply -f k8s-deployment.yaml
kubectl get pods -l app=sathishlogger
kubectl get service sathishlogger-service
```

### Helm Chart (Advanced)

```yaml
# helm/values.yaml
replicaCount: 3

image:
  repository: sathishlogger
  tag: latest
  pullPolicy: IfNotPresent

service:
  type: LoadBalancer
  port: 80
  targetPort: 8080

ingress:
  enabled: true
  annotations:
    kubernetes.io/ingress.class: nginx
    cert-manager.io/cluster-issuer: letsencrypt-prod
  hosts:
    - host: sathishlogger.yourdomain.com
      paths:
        - path: /
          pathType: Prefix
  tls:
    - secretName: sathishlogger-tls
      hosts:
        - sathishlogger.yourdomain.com

config:
  database:
    url: "jdbc:postgresql://postgres-service:5432/sathishlogger"
    username: "sathish"
    password: "secure-password"
  logging:
    level: "INFO"
  storage:
    type: "database"

resources:
  limits:
    cpu: 500m
    memory: 1Gi
  requests:
    cpu: 250m
    memory: 512Mi

autoscaling:
  enabled: true
  minReplicas: 2
  maxReplicas: 10
  targetCPUUtilizationPercentage: 80
```

## 🗄️ Database Configurations

### PostgreSQL (Recommended for Production)

```yaml
# docker-compose.yml for production
version: '3.8'
services:
  sathishlogger:
    image: sathishlogger:latest
    environment:
      - DATABASE_URL=jdbc:postgresql://postgres:5432/sathishlogger
      - DATABASE_USERNAME=sathish
      - DATABASE_PASSWORD=${DB_PASSWORD}
      - JPA_DDL_AUTO=update
      - LOG_LEVEL=WARN
    depends_on:
      - postgres
  
  postgres:
    image: postgres:15-alpine
    environment:
      - POSTGRES_DB=sathishlogger
      - POSTGRES_USER=sathish
      - POSTGRES_PASSWORD=${DB_PASSWORD}
    volumes:
      - postgres_data:/var/lib/postgresql/data
      - ./init.sql:/docker-entrypoint-initdb.d/init.sql
    ports:
      - "5432:5432"

volumes:
  postgres_data:
```

### MySQL Configuration

```bash
# Environment variables for MySQL
DATABASE_URL=jdbc:mysql://mysql:3306/sathishlogger?useSSL=false&serverTimezone=UTC
DATABASE_USERNAME=sathish
DATABASE_PASSWORD=secure-password
DATABASE_DRIVER=com.mysql.cj.jdbc.Driver
HIBERNATE_DIALECT=org.hibernate.dialect.MySQL8Dialect
```

### Cloud Database Services

#### AWS RDS PostgreSQL
```bash
DATABASE_URL=jdbc:postgresql://sathishlogger.cluster-xyz.us-east-1.rds.amazonaws.com:5432/sathishlogger
DATABASE_USERNAME=sathish
DATABASE_PASSWORD=secure-password
```

#### Google Cloud SQL
```bash
DATABASE_URL=jdbc:postgresql://google/sathishlogger?cloudSqlInstance=project:region:instance&socketFactory=com.google.cloud.sql.postgres.SocketFactory
DATABASE_USERNAME=sathish
DATABASE_PASSWORD=secure-password
```

## 🔧 Environment-Specific Configurations

### Development Environment
```bash
# .env.dev
SERVER_PORT=8080
DATABASE_URL=jdbc:h2:mem:sathishlogger
JPA_DDL_AUTO=create-drop
JPA_SHOW_SQL=true
LOG_LEVEL=DEBUG
H2_CONSOLE_ENABLED=true
```

### Staging Environment
```bash
# .env.staging
SERVER_PORT=8080
DATABASE_URL=jdbc:postgresql://staging-db:5432/sathishlogger
DATABASE_USERNAME=sathish
DATABASE_PASSWORD=staging-password
JPA_DDL_AUTO=update
LOG_LEVEL=INFO
API_KEY_ENABLED=true
API_KEY=staging-api-key
```

### Production Environment
```bash
# .env.prod
SERVER_PORT=8080
DATABASE_URL=jdbc:postgresql://prod-db:5432/sathishlogger
DATABASE_USERNAME=sathish
DATABASE_PASSWORD=super-secure-password
JPA_DDL_AUTO=validate
LOG_LEVEL=WARN
API_KEY_ENABLED=true
API_KEY=production-api-key
LOG_RETENTION_DAYS=90
```

## 📊 Monitoring & Observability

### Prometheus Metrics
```yaml
# prometheus.yml
scrape_configs:
  - job_name: 'sathishlogger'
    static_configs:
      - targets: ['sathishlogger:8080']
    metrics_path: '/actuator/prometheus'
    scrape_interval: 15s
```

### Grafana Dashboard
```json
{
  "dashboard": {
    "title": "SathishLogger Metrics",
    "panels": [
      {
        "title": "Log Ingestion Rate",
        "type": "graph",
        "targets": [
          {
            "expr": "rate(http_requests_total{job=\"sathishlogger\",uri=\"/api/logs/log\"}[5m])"
          }
        ]
      },
      {
        "title": "Error Rate by Application",
        "type": "graph",
        "targets": [
          {
            "expr": "sum by (application_name) (rate(log_entries_total{log_level=\"ERROR\"}[5m]))"
          }
        ]
      }
    ]
  }
}
```

## 🔒 Security Best Practices

### 1. Network Security
```yaml
# docker-compose with network isolation
version: '3.8'
services:
  sathishlogger:
    networks:
      - backend
  postgres:
    networks:
      - backend
networks:
  backend:
    driver: bridge
    internal: true
```

### 2. API Key Authentication
```bash
# Enable API key authentication
API_KEY_ENABLED=true
API_KEY=your-super-secret-api-key-here

# Client usage with API key
curl -X POST http://localhost:8080/api/logs/log \
  -H "Content-Type: application/json" \
  -H "X-API-Key: your-super-secret-api-key-here" \
  -d '{"applicationName":"secure-app","logLevel":"INFO","message":"Authenticated log"}'
```

### 3. HTTPS/TLS Configuration
```yaml
# nginx.conf for HTTPS termination
server {
    listen 443 ssl;
    server_name sathishlogger.yourdomain.com;
    
    ssl_certificate /etc/ssl/certs/sathishlogger.crt;
    ssl_certificate_key /etc/ssl/private/sathishlogger.key;
    
    location / {
        proxy_pass http://sathishlogger:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

## 🚀 Scaling Strategies

### Horizontal Pod Autoscaler (HPA)
```yaml
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: sathishlogger-hpa
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: sathishlogger
  minReplicas: 2
  maxReplicas: 20
  metrics:
  - type: Resource
    resource:
      name: cpu
      target:
        type: Utilization
        averageUtilization: 70
  - type: Resource
    resource:
      name: memory
      target:
        type: Utilization
        averageUtilization: 80
```

### Load Balancing with HAProxy
```
# haproxy.cfg
global
    daemon

defaults
    mode http
    timeout connect 5000ms
    timeout client 50000ms
    timeout server 50000ms

frontend sathishlogger_frontend
    bind *:80
    default_backend sathishlogger_backend

backend sathishlogger_backend
    balance roundrobin
    server sathishlogger1 sathishlogger-1:8080 check
    server sathishlogger2 sathishlogger-2:8080 check
    server sathishlogger3 sathishlogger-3:8080 check
```

## 📋 Deployment Checklist

### Pre-Deployment
- [ ] Review and customize `.env` file
- [ ] Set up database (PostgreSQL recommended for production)
- [ ] Configure network security and firewall rules
- [ ] Set up SSL/TLS certificates
- [ ] Configure monitoring and alerting
- [ ] Set up backup strategy for database

### Deployment
- [ ] Build and test Docker image
- [ ] Deploy to staging environment first
- [ ] Run integration tests
- [ ] Deploy to production
- [ ] Verify health checks pass
- [ ] Test log ingestion and retrieval

### Post-Deployment
- [ ] Monitor application metrics
- [ ] Set up log retention policies
- [ ] Configure alerting for errors
- [ ] Document service endpoints for teams
- [ ] Set up regular backups
- [ ] Plan for scaling based on usage

---

**Ready to deploy SathishLogger across all your projects!** 🚀
