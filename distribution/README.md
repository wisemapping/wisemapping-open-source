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

## Recommended Deployment

For most scenarios, pull the official image from Docker Hub and configure it with environment variables or mounted `application.yml`:

```bash
docker pull wisemapping/wisemapping:latest
docker run -d \
  --name wisemapping \
  -p 80:80 \
  wisemapping/wisemapping:latest
```

See the published image page for tag details and configuration examples: https://hub.docker.com/r/wisemapping/wisemapping

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

## Quick Comparison

| Feature | API Only | Full Stack |
|---------|----------|------------|
| Backend API | ✅ | ✅ |
| Frontend UI | ❌ | ✅ |
| Database | External | HSQLDB (in-memory) |
| Container Count | 1 | 1 |
| Ports | 8080 | 80 (8080 internal) |
| Data Persistence | N/A | ⚠️ Not recommended |
| Best For | Microservices | Quick testing |
| Production Ready | ✅ | ⚠️ Not for data |

**Note**: For frontend-only deployments, use the `@wisemapping/webapp` npm package with any static hosting service.

## Deployment Strategies

### Development / Testing
Use **Full Stack** for quick local setup:
```bash
docker build -f distribution/app/Dockerfile -t wisemapping:latest .
docker run -d -p 80:80 wisemapping:latest
```

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

- **JAVA_OPTS**: JVM options (defaults to `-XX:InitialRAMPercentage=60 -XX:MaxRAMPercentage=70`; override to customize)
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

Both published images are **multi-architecture** (`linux/amd64` + `linux/arm64`),
so they run natively on Apple Silicon, AWS Graviton, Raspberry Pi and other
ARM64 hosts without emulation.

`.github/workflows/docker-api-publish.yml` builds the **API Only** configuration:
- Builds the JAR with Maven
- Packages it using `distribution/api/Dockerfile`
- Cross-builds both architectures in one job under QEMU (the image only copies
  an architecture-neutral JAR, so emulation is cheap)
- Publishes to DigitalOcean Container Registry

`.github/workflows/docker-app-publish.yml` builds the **Full Stack** image:
- Builds each architecture natively on its own runner (`ubuntu-latest` for
  amd64, `ubuntu-24.04-arm` for arm64), because the image compiles the backend
  with Maven and the frontend with webpack — far too slow under emulation
- Pushes each build by digest, then merges them into a single tagged manifest
  list with `docker buildx imagetools create`
- Publishes to Docker Hub as `wisemapping/wisemapping`

## Building for Production

### With New Relic Monitoring

```bash
docker build -f distribution/api/Dockerfile \
  --build-arg ENABLE_NEWRELIC=true \
  -t wisemapping-api:latest .
```

### Multi-Architecture Builds

A multi-platform build cannot be loaded into the local Docker daemon, so it must
be pushed straight to a registry (`--push`) or exported to a local
OCI layout (`--output type=oci,dest=...`):

```bash
docker buildx build \
  --platform linux/amd64,linux/arm64 \
  -f distribution/api/Dockerfile \
  -t <registry>/wisemapping-api:latest \
  --push .
```

To check which architectures a published tag provides:

```bash
docker buildx imagetools inspect wisemapping/wisemapping:latest
```

## License

Copyright [2007-2025] [wisemapping]

Licensed under WiseMapping Public License, Version 1.0 (the "License").
See https://github.com/wisemapping/wisemapping-open-source/blob/main/LICENSE.md

