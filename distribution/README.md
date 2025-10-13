# WiseMapping Distribution

This directory contains Docker configurations for deploying WiseMapping in various configurations.

## Directory Structure

```
distribution/
├── api/
│   ├── Dockerfile          # Backend API only
│   └── env-config.sh       # Environment configuration script
└── app/
    ├── Dockerfile          # Full stack (Frontend + Backend)
    ├── nginx.conf          # Nginx configuration
    ├── supervisord.conf    # Supervisor config for process management
    ├── .dockerignore       # Docker ignore patterns
    └── README.md           # Full stack documentation
```

## Available Configurations

### 1. Backend API Only (`distribution/api/Dockerfile`)

**Use case**: When you want to run only the backend API, typically paired with a separately deployed frontend or when using frontend from CDN/npm package.

**Build**:
```bash
# From repository root
mvn clean package -f wise-api/pom.xml
docker build -f distribution/api/Dockerfile -t wisemapping-api:latest .
```

**Run**:
```bash
docker run -d \
  --name wisemapping-api \
  -p 8080:8080 \
  wisemapping-api:latest
```

**GitHub Actions**: This is the configuration used in CI/CD workflows.

**Frontend Deployment**: The frontend is published as `@wisemapping/webapp` npm package and can be deployed to any static hosting service (Vercel, Netlify, S3, CDN, etc.).

### 2. Full Stack (`distribution/app/Dockerfile`)

**Use case**: All-in-one deployment with both frontend and backend in a single container.

**How it works**: 
- Clones frontend from GitHub repository (main branch by default)
- Builds both frontend and backend from source
- Packages everything into a single container

**Build**:
```bash
# From repository root
docker build -f distribution/app/Dockerfile -t wisemapping:latest .

# Or build with a specific frontend branch/tag
docker build -f distribution/app/Dockerfile \
  --build-arg FRONTEND_BRANCH=develop \
  -t wisemapping:develop .
```

**Run**:
```bash
docker run -d \
  --name wisemapping \
  -p 80:80 \
  wisemapping:latest
```

**Documentation**: See [README.md](app/README.md) for detailed instructions.

### 3. Full Stack with PostgreSQL (`distribution/app-postgresql/`)

**Use case**: Production-ready deployment with PostgreSQL database using Docker Compose.

**How it works**:
- Uses Docker Compose to orchestrate WiseMapping + PostgreSQL
- PostgreSQL database with persistent storage
- Automatic schema initialization
- Health checks and proper service dependencies

**Run**:
```bash
# From repository root
cd distribution/app-postgresql
docker-compose up -d
```

**Features**:
- ✅ Production-grade PostgreSQL database
- ✅ Persistent data storage
- ✅ Automatic database initialization
- ✅ Health checks and dependency management
- ✅ Easy backup and restore
- ✅ Configurable via YAML files

**Access**:
- Application: http://localhost
- Default credentials: test@wisemapping.org / test

**Documentation**: See [app-postgresql/README.md](app-postgresql/README.md) for detailed instructions.

## Quick Comparison

| Feature | API Only | Full Stack | Full Stack + PostgreSQL |
|---------|----------|------------|------------------------|
| Backend API | ✅ | ✅ | ✅ |
| Frontend UI | ❌ | ✅ | ✅ |
| Database | External | HSQLDB (in-memory) | PostgreSQL (persistent) |
| Container Count | 1 | 1 | 2 |
| Ports | 8080 | 80 (8080 internal) | 80 + 5432 |
| Data Persistence | N/A | ⚠️ Not recommended | ✅ Production ready |
| Best For | Microservices | Quick testing | Production deployment |
| Production Ready | ✅ | ⚠️ Not for data | ✅ Recommended |

**Note**: For frontend-only deployments, use the `@wisemapping/webapp` npm package with any static hosting service.

## Deployment Strategies

### Development / Testing
Use **Full Stack** for quick local setup:
```bash
docker build -f distribution/app/Dockerfile -t wisemapping:latest .
docker run -d -p 80:80 wisemapping:latest
```

### Production (Recommended - PostgreSQL)
Use **Full Stack with PostgreSQL** for production deployment:
```bash
cd distribution/app-postgresql
docker-compose up -d
```

**Why this is recommended:**
- ✅ Production-grade PostgreSQL database
- ✅ Persistent data storage with Docker volumes
- ✅ Easy backup and restore capabilities
- ✅ Health checks and dependency management
- ✅ Scalable and maintainable

### Production (Microservices)
Deploy **API** separately and use frontend from npm package:
```bash
# Backend
docker build -f distribution/api/Dockerfile -t wisemapping-api:latest .
docker run -d -p 8080:8080 wisemapping-api:latest

# Frontend - deploy @wisemapping/webapp to CDN, Vercel, Netlify, S3, etc.
```

### Production (Single Container with External Database)
Use **Full Stack** with external PostgreSQL/MySQL:
```bash
docker build -f distribution/app/Dockerfile -t wisemapping:latest .
docker run -d -p 80:80 \
  -v $(pwd)/app.yml:/app/config/app.yml:ro \
  wisemapping:latest
```

## Environment Configuration

All configurations support environment-based configuration:

- **JAVA_OPTS**: JVM options (e.g., `-Xmx1024m`)
- **SPRING_CONFIG_ADDITIONAL_FILE_CONTENT**: Spring Boot YAML configuration
- **NEW_RELIC_OPTS**: New Relic agent options (if enabled at build time)
- **NEW_RELIC_CONFIG_FILE_CONTENT**: New Relic configuration

## Database Configuration

By default, all configurations use an in-memory HSQLDB database. For production, configure an external database:

```bash
docker run -d \
  -p 80:80 \
  -e SPRING_CONFIG_ADDITIONAL_FILE_CONTENT="
spring:
  datasource:
    url: jdbc:postgresql://postgres:5432/wisemapping
    username: wisemapping
    password: password
    driver-class-name: org.postgresql.Driver
" \
  wisemapping:latest
```

## CI/CD Integration

The GitHub Actions workflow (`.github/workflows/docker-publish.yml`) uses the **API Only** configuration:
- Builds the JAR with Maven
- Packages it using `distribution/api/Dockerfile`
- Publishes to DigitalOcean Container Registry

## Building for Production

### With New Relic Monitoring

```bash
docker build -f distribution/api/Dockerfile \
  --build-arg ENABLE_NEWRELIC=true \
  -t wisemapping-api:latest .
```

### Multi-Architecture Builds

```bash
docker buildx build \
  --platform linux/amd64,linux/arm64 \
  -f distribution/api/Dockerfile \
  -t wisemapping-api:latest .
```

## License

Copyright [2007-2025] [wisemapping]

Licensed under WiseMapping Public License, Version 1.0 (the "License").
See https://github.com/wisemapping/wisemapping-open-source/blob/main/LICENSE.md

