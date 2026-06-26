# SathishLogger - Centralized Logging Service

A **parameterized Docker-based logging service** that you can deploy once and use across all your projects. SathishLogger provides REST APIs for log ingestion, correlation ID tracking, and log aggregation with configurable storage options.

## 🚀 Quick Start

### 1. Deploy with Docker Compose (Recommended)

```bash
# Clone and setup
git clone <your-repo>
cd verbose-parakeet

# Copy environment file and customize
cp .env.example .env
# Edit .env with your preferences

# Start the service
docker-compose up -d

# Check health
curl http://localhost:8080/api/logs/health
```

### 2. Deploy with Docker Only

```bash
# Build the image
docker build -t sathishlogger .

# Run with environment variables
docker run -d \
  --name sathishlogger \
  -p 8080:8080 \
  -e DATABASE_URL=jdbc:h2:mem:sathishlogger \
  -e LOG_LEVEL=INFO \
  -e STORAGE_TYPE=database \
  -v ./logs:/app/logs \
  sathishlogger
```

## 📊 Features

- **REST API** for log ingestion from any application
- **Correlation ID tracking** for request tracing
- **Multiple storage options**: Database (H2/PostgreSQL), File, or External
- **Batch logging** support for high-throughput scenarios
- **Query APIs** for log retrieval and analysis
- **Health checks** and metrics via Spring Actuator
- **Multi-language client SDKs** (Java, Python, Node.js)
- **Fully parameterized** via environment variables

## 🔧 Configuration Parameters

| Environment Variable | Default | Description |
|---------------------|---------|-------------|
| `SERVER_PORT` | `8080` | Server port |
| `DATABASE_URL` | `jdbc:h2:mem:sathishlogger` | Database connection URL |
| `DATABASE_USERNAME` | `sa` | Database username |
| `DATABASE_PASSWORD` | `` | Database password |
| `LOG_LEVEL` | `INFO` | Application log level |
| `STORAGE_TYPE` | `database` | Storage type: `database`, `file`, or `external` |
| `LOG_RETENTION_DAYS` | `30` | Log retention period |
| `API_KEY_ENABLED` | `false` | Enable API key authentication |
| `API_KEY` | `` | API key for authentication |

## 📡 API Endpoints

### Log Ingestion
```bash
# Single log entry
POST /api/logs/log
Content-Type: application/json

{
  "applicationName": "my-app",
  "logLevel": "INFO",
  "message": "Application started",
  "correlationId": "uuid-here",
  "metadata": {"key": "value"}
}

# Batch logging
POST /api/logs/batch
Content-Type: application/json

[
  {"applicationName": "my-app", "logLevel": "INFO", "message": "Log 1"},
  {"applicationName": "my-app", "logLevel": "ERROR", "message": "Log 2"}
]
```

### Log Retrieval
```bash
# Get logs by application
GET /api/logs/application/my-app?page=0&size=50

# Get logs by correlation ID
GET /api/logs/correlation/uuid-here

# Search logs with filters
GET /api/logs/search?applicationName=my-app&startTime=2023-01-01T00:00:00&endTime=2023-12-31T23:59:59

# Get application statistics
GET /api/logs/stats/my-app

# List all applications
GET /api/logs/applications
```

## 💻 Client Integration

### Java
```java
// Add SathishLoggerClient.java to your project
SathishLoggerClient logger = new SathishLoggerClient("http://localhost:8080", "my-app");

logger.info("Application started");
logger.error("Something went wrong", exception);
logger.info("Processing request", correlationId);
```

### Python
```python
# Use the Python client
from sathishlogger_client import SathishLoggerClient

logger = SathishLoggerClient("http://localhost:8080", "python-app")
logger.info("Application started")
logger.error("An error occurred", exception=e)
```

### Node.js
```javascript
const SathishLoggerClient = require('./sathishlogger-client');

const logger = new SathishLoggerClient('http://localhost:8080', 'nodejs-app');
await logger.info('Application started');
await logger.error('An error occurred', correlationId, error);
```

### cURL (Any Language)
```bash
curl -X POST http://localhost:8080/api/logs/log \
  -H "Content-Type: application/json" \
  -d '{
    "applicationName": "my-app",
    "logLevel": "INFO", 
    "message": "Hello from cURL"
  }'
```

## 🐳 Deployment Options

### Production with PostgreSQL
```yaml
# docker-compose.yml
version: '3.8'
services:
  sathishlogger:
    image: sathishlogger:latest
    environment:
      - DATABASE_URL=jdbc:postgresql://postgres:5432/sathishlogger
      - DATABASE_USERNAME=sathish
      - DATABASE_PASSWORD=secure-password
      - JPA_DDL_AUTO=update
      - LOG_LEVEL=WARN
    depends_on:
      - postgres
  
  postgres:
    image: postgres:15-alpine
    environment:
      - POSTGRES_DB=sathishlogger
      - POSTGRES_USER=sathish
      - POSTGRES_PASSWORD=secure-password
```

### Kubernetes Deployment
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: sathishlogger
spec:
  replicas: 2
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
        env:
        - name: DATABASE_URL
          value: "jdbc:postgresql://postgres-service:5432/sathishlogger"
        - name: LOG_LEVEL
          value: "INFO"
---
apiVersion: v1
kind: Service
metadata:
  name: sathishlogger-service
spec:
  selector:
    app: sathishlogger
  ports:
  - port: 8080
    targetPort: 8080
  type: LoadBalancer
```

## 🔍 Monitoring & Health

### Health Check
```bash
curl http://localhost:8080/api/logs/health
# Response: {"status":"UP","service":"SathishLogger"}
```

### Metrics (Actuator)
```bash
curl http://localhost:8080/actuator/health
curl http://localhost:8080/actuator/metrics
curl http://localhost:8080/actuator/prometheus
```

### Log Analysis
```bash
# View recent errors across all apps
curl "http://localhost:8080/api/logs/level/ERROR?size=100"

# Get stats for specific application
curl "http://localhost:8080/api/logs/stats/my-app"
```

## 🛠️ Development

### Build Locally
```bash
# Build with Maven
mvn clean package

# Run locally
java -jar target/sathishlogger-*.jar

# Or with Docker
docker build -t sathishlogger .
docker run -p 8080:8080 sathishlogger
```

### Testing
```bash
# Run tests
mvn test

# Test the API
curl -X POST http://localhost:8080/api/logs/log \
  -H "Content-Type: application/json" \
  -d '{"applicationName":"test","logLevel":"INFO","message":"Test log"}'
```

## 📁 Project Structure

```
verbose-parakeet/
├── src/main/java/com/sathish/sathishlogger/
│   ├── SathishLoggerApplication.java
│   ├── controller/LoggingController.java
│   ├── service/LoggingService.java
│   ├── model/LogEntry.java
│   ├── repository/LogEntryRepository.java
│   └── dto/LogRequest.java
├── client-sdk/
│   ├── SathishLoggerClient.java
│   ├── example-usage.py
│   └── example-usage.js
├── docker-compose.yml
├── Dockerfile
├── .env.example
└── README.md
```

## 🚀 Usage in Your Projects

1. **Deploy SathishLogger** once using Docker/Kubernetes
2. **Copy client SDK** to your projects
3. **Initialize client** with SathishLogger URL and your app name
4. **Start logging** with correlation IDs for request tracing

### Example Integration
```java
// In your Spring Boot app
@RestController
public class UserController {
    private final SathishLoggerClient logger = 
        new SathishLoggerClient("http://sathishlogger:8080", "user-service");
    
    @GetMapping("/users/{id}")
    public User getUser(@PathVariable String id, HttpServletRequest request) {
        String correlationId = request.getHeader("X-Correlation-ID");
        
        logger.info("Fetching user: " + id, correlationId);
        
        try {
            User user = userService.findById(id);
            logger.info("User found: " + id, correlationId);
            return user;
        } catch (Exception e) {
            logger.error("Failed to fetch user: " + id, correlationId, e);
            throw e;
        }
    }
}
```

## 🔒 Security

- **API Key Authentication**: Set `API_KEY_ENABLED=true` and provide `API_KEY`
- **Network Security**: Deploy in private network, use HTTPS in production
- **Data Privacy**: Configure log retention and consider PII masking

## 📈 Scaling

- **Horizontal Scaling**: Run multiple SathishLogger instances behind load balancer
- **Database Scaling**: Use PostgreSQL with read replicas
- **Async Logging**: Use client async methods for high-throughput scenarios
- **Batch Processing**: Use `/batch` endpoint for bulk log ingestion

---

**SathishLogger** - One logging service for all your projects! 🎯
