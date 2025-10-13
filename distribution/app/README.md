# WiseMapping Full Stack Docker Image

This directory contains a multi-stage Dockerfile that builds both the WiseMapping backend (Spring Boot API) and frontend (React UI) into a single, self-contained Docker image.

## Quick Reference

**Quick start with persistent data:**
```bash
# Build
docker build -f distribution/app/Dockerfile -t wisemapping:latest .

# Create app.yml configuration
cat > app.yml <<EOF
spring:
  datasource:
    url: jdbc:hsqldb:file:/var/lib/wisemapping/db/wisemapping;sql.names=false;sql.regular_names=false;shutdown=true
    driver-class-name: org.hsqldb.jdbc.JDBCDriver
    username: sa
    password: ''
  jpa:
    hibernate:
      ddl-auto: update
EOF

# Run with persistent HSQLDB
docker run -d --name wisemapping -p 80:80 \
  -v wisemapping-db:/var/lib/wisemapping \
  -v $(pwd)/app.yml:/app/config/app.yml:ro \
  wisemapping:latest

# Access at http://localhost (Frontend and API on same port)
```

**Important paths:**
- Configuration file: `/app/config/app.yml` (mount your own file here)
- Database files: `/var/lib/wisemapping/db/` (when using persistent HSQLDB)
- Application runs as user: `wisemapping` (UID 1001)

**Important ports:**
- Port 80: All traffic (Nginx routes to frontend or backend based on path)

**Access:**
- **Frontend**: `http://localhost`
- **API**: `http://localhost/api`
- **Health check**: `http://localhost/health`

**How it works:**
- Everything goes through port 80
- Nginx routes based on path:
  - `/api/*` → Proxied to Spring Boot backend (port 8080 internal)
  - `/c/*` → Frontend React app
  - `/*` → Frontend React app
- Backend only listens on internal port 8080 (not exposed outside container)

## Features

- **Multi-stage build**: Optimized image size by separating build and runtime stages
- **Built from source**: Clones and builds frontend from GitHub repository
- **All-in-one**: Both frontend and backend in a single container
- **Production-ready**: Uses Nginx as reverse proxy and Supervisor for process management
- **Configurable**: Simple configuration via mounted app.yml file
- **Flexible versioning**: Build with any branch or tag from the frontend repository

## Architecture

The Docker build process:
1. **Stage 1**: Builds backend JAR from `wise-api/` using Maven
2. **Stage 2**: Clones frontend repo from GitHub and builds with Yarn
3. **Stage 3**: Combines both into runtime image with Nginx + Supervisor

The final image contains:
- **Nginx** (port 80): Serves frontend static files and proxies API requests
- **Spring Boot API** (port 8080): Backend REST API
- **Supervisor**: Manages both processes

## Quick Start

### Build the Image

From the repository root:

```bash
docker build -f distribution/app/Dockerfile -t wisemapping:latest .
```

**Note**: 
- The build process clones and compiles the frontend from GitHub, which may take 5-10 minutes depending on your connection and hardware.
- By default, Docker may cache the git clone step. To ensure you get the latest code, use the `CACHEBUST` build argument.

**Build with latest code (recommended):**
```bash
docker build -f distribution/app/Dockerfile \
  --build-arg CACHEBUST=$(date +%s) \
  -t wisemapping:latest .
```

**Build with specific frontend branch:**
```bash
docker build -f distribution/app/Dockerfile \
  --build-arg FRONTEND_BRANCH=develop \
  --build-arg CACHEBUST=$(date +%s) \
  -t wisemapping:develop .
```

**Force complete rebuild (ignore all Docker cache):**
```bash
docker build --no-cache -f distribution/app/Dockerfile -t wisemapping:latest .
```

### Run the Container

#### Basic run with in-memory HSQLDB (NOT recommended for production):

> **⚠️ PRODUCTION WARNING**: HSQLDB is **NOT recommended for production environments**. Use PostgreSQL or MySQL for production deployments.

```bash
docker run -d \
  --name wisemapping \
  -p 80:80 \
  wisemapping:latest
```

Access the application at:
- **Frontend**: http://localhost
- **API**: http://localhost/api

**⚠️ Warning**: Data will be lost when the container is stopped/removed.

#### Run with persistent HSQLDB (Only for testing/small personal deployments):

First, create an `app.yml` configuration file:

```yaml
# app.yml
spring:
  datasource:
    url: jdbc:hsqldb:file:/var/lib/wisemapping/db/wisemapping;sql.names=false;sql.regular_names=false;shutdown=true
    driver-class-name: org.hsqldb.jdbc.JDBCDriver
    username: sa
    password: ''
  jpa:
    hibernate:
      ddl-auto: update
```

Then run the container:

```bash
docker run -d \
  --name wisemapping \
  -p 80:80 \
  -v wisemapping-db:/var/lib/wisemapping \
  -v $(pwd)/app.yml:/app/config/app.yml:ro \
  wisemapping:latest
```

This will:
- Create a Docker volume named `wisemapping-db`
- Mount your `app.yml` configuration file
- Persist the database files to `/var/lib/wisemapping/db/` inside the container
- Data survives container restarts and removals

### Run with External Database (PostgreSQL)

Create an `app.yml` configuration file:

```yaml
# app.yml
spring:
  datasource:
    url: jdbc:postgresql://postgres:5432/wisemapping
    username: wisemapping
    password: password
    driver-class-name: org.postgresql.Driver
  jpa:
    hibernate:
      ddl-auto: validate
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
```

Then run:

```bash
docker run -d \
  --name wisemapping \
  -p 80:80 \
  -v $(pwd)/app.yml:/app/config/app.yml:ro \
  wisemapping:latest
```

## Architecture

### Single Port Design

Everything runs on **port 80** with Nginx handling intelligent routing:

```
User Request → Port 80 (Nginx) → Routes based on path:
                                  ├─ /api/*  → Spring Boot (internal port 8080)
                                  ├─ /c/*    → Frontend (React)
                                  └─ /*      → Frontend (React)
```

**Frontend Configuration Flow:**
1. **Build time**: Frontend compiled with `window.BoostrapConfig` pointing to `/api/restful/app/config`
2. **Runtime**: Frontend loads → Fetches config from `/api/restful/app/config` (relative URL)
3. **Nginx proxies** → Routes to Spring Boot backend
4. **Backend responds** → Returns full configuration (API base URL, OAuth settings, etc.)
5. **Frontend uses config** → All API calls go through `/api/*` (relative paths)

**Benefits:**
- ✅ **Zero configuration needed** - Works out of the box
- ✅ **Simpler deployment** - Only one port to expose
- ✅ **No CORS issues** - Same origin for frontend and API
- ✅ **Standard web architecture** - Works like traditional web apps
- ✅ **Easier firewall rules** - Only port 80 (or 443 with SSL)
- ✅ **Production ready** - Industry standard pattern

## Configuration

### Application Configuration

Mount your Spring Boot configuration file at `/app/config/app.yml`:

```bash
docker run -d \
  -p 80:80 \
  -v $(pwd)/app.yml:/app/config/app.yml:ro \
  wisemapping:latest
```

The configuration file is optional. If not provided, WiseMapping will use default settings with in-memory HSQLDB.

### Environment Variables

#### Java Options
- `JAVA_OPTS`: JVM options (e.g., `-Xmx1024m -Xms512m`)

Example:
```bash
docker run -d -p 80:80 \
  -e JAVA_OPTS="-Xmx2048m -Xms1024m" \
  wisemapping:latest
```


## Configuration Examples

### MySQL/MariaDB

```yaml
spring:
  datasource:
    url: jdbc:mysql://mysql:3306/wisemapping?useUnicode=true&characterEncoding=utf8
    username: wisemapping
    password: password
    driver-class-name: com.mysql.cj.jdbc.Driver
  jpa:
    hibernate:
      ddl-auto: validate
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQLDialect
```

### Custom Application Properties

```yaml
app:
  admin:
    user: admin@wisemapping.com
  mail:
    sender-email: noreply@wisemapping.com
  security:
    oauth2:
      google:
        enabled: true
        clientId: YOUR_CLIENT_ID
        clientSecret: YOUR_CLIENT_SECRET
```

## Health Checks

- **Application Health**: `http://localhost/health`
- **API Health**: `http://localhost/api/actuator/health` (if actuator is enabled)

## Logs

View logs:
```bash
docker logs -f wisemapping
```

View specific process logs:
```bash
# Backend logs
docker exec wisemapping tail -f /var/log/supervisor/spring-boot-stdout---supervisor-*.log

# Nginx logs
docker exec wisemapping tail -f /var/log/nginx/access.log
```

## Volume Management

### Backup HSQLDB Data

To backup your HSQLDB data:

```bash
# Stop the container gracefully
docker stop wisemapping

# Create a backup
docker run --rm \
  -v wisemapping-db:/data \
  -v $(pwd):/backup \
  alpine tar czf /backup/wisemapping-backup-$(date +%Y%m%d).tar.gz -C /data .

# Start the container again
docker start wisemapping
```

### Restore HSQLDB Data

To restore from a backup:

```bash
# Stop and remove the container
docker stop wisemapping
docker rm wisemapping

# Restore the data
docker run --rm \
  -v wisemapping-db:/data \
  -v $(pwd):/backup \
  alpine sh -c "cd /data && tar xzf /backup/wisemapping-backup-YYYYMMDD.tar.gz"

# Start the container again
docker run -d \
  --name wisemapping \
  -p 80:80 \
  -v wisemapping-db:/var/lib/wisemapping \
  -e SPRING_CONFIG_ADDITIONAL_FILE_CONTENT="..." \
  wisemapping:latest
```

### Using Host Directory Instead of Volume

If you prefer to use a host directory for easier access:

```bash
# Create directory on host
mkdir -p /path/to/wisemapping-data

# Create app.yml (pointing to the mounted path)
cat > app.yml <<EOF
spring:
  datasource:
    url: jdbc:hsqldb:file:/var/lib/wisemapping/db/wisemapping;sql.names=false;sql.regular_names=false;shutdown=true
    driver-class-name: org.hsqldb.jdbc.JDBCDriver
    username: sa
    password: ''
  jpa:
    hibernate:
      ddl-auto: update
EOF

# Run with host mount
docker run -d \
  --name wisemapping \
  -p 80:80 \
  -v /path/to/wisemapping-data:/var/lib/wisemapping \
  -v $(pwd)/app.yml:/app/config/app.yml:ro \
  wisemapping:latest
```

Database files will be in `/path/to/wisemapping-data/db/` on your host system.

### Inspect Volume

To see where Docker stores the volume data:

```bash
docker volume inspect wisemapping-db
```

To list all files in the volume:

```bash
docker run --rm -v wisemapping-db:/data alpine ls -lah /data/db/
```

## Troubleshooting

### Port Already in Use
If port 80 is already in use, map to a different port:
```bash
docker run -d -p 8080:80 wisemapping:latest
```

### Database Connection Issues
- Ensure the database is running and accessible
- Check database credentials in `SPRING_CONFIG_ADDITIONAL_FILE_CONTENT`
- Verify network connectivity between containers
- For HSQLDB: Ensure the volume is properly mounted and writable

### Database Corruption (HSQLDB)
If HSQLDB database becomes corrupted:
```bash
# Stop the container
docker stop wisemapping

# Backup current data (just in case)
docker run --rm -v wisemapping-db:/data -v $(pwd):/backup alpine tar czf /backup/corrupted-backup.tar.gz -C /data .

# Remove corrupted database files
docker run --rm -v wisemapping-db:/data alpine sh -c "rm -rf /data/db/*"

# Restart container (will create fresh database)
docker start wisemapping
```

### Memory Issues
Increase JVM heap size:
```bash
docker run -d -e JAVA_OPTS="-Xmx2048m -Xms1024m" wisemapping:latest
```

### Permission Issues with Volumes
If you encounter permission issues with host-mounted directories:
```bash
# Change ownership to match container user (UID 1001)
sudo chown -R 1001:1001 /path/to/wisemapping-data
```

### Running Interactively for Debugging

To run the container interactively and see all logs in real-time:

```bash
docker run -it --rm \
  --name wisemapping \
  -p 80:80 \
  wisemapping:latest
```

This will:
- Run in foreground (no `-d` flag)
- Show all logs from both Nginx and Spring Boot
- Remove container on exit (`--rm`)
- Allow you to stop with `Ctrl+C`

**Run with shell access (for debugging):**

```bash
docker run -it --rm \
  --name wisemapping \
  -p 80:80 \
  wisemapping:latest \
  /bin/sh
```

This gives you shell access inside the container. To start the services manually:
```bash
# Inside container
/usr/bin/supervisord -c /etc/supervisord.conf
```

**Attach to running container:**

```bash
# Get shell in running container
docker exec -it wisemapping /bin/sh

# View Spring Boot logs
docker exec -it wisemapping tail -f /var/log/supervisor/spring-boot-stdout*

# View Nginx logs
docker exec -it wisemapping tail -f /var/log/nginx/access.log
```

## Security Considerations

1. **Change default credentials**: Always update default admin credentials after first login
2. **Use external database**: Don't use in-memory HSQLDB for production
3. **Secure secrets**: Use Docker secrets or environment variables for sensitive data
4. **HTTPS**: Use a reverse proxy (e.g., Traefik, Nginx) for SSL termination
5. **Non-root user**: The application runs as the `wisemapping` user (UID 1001)

## Building with Custom Frontend Branch

The Dockerfile clones and builds the frontend from the GitHub repository. By default, it uses the `main` branch.

To build with a different branch or tag:

```bash
docker build -f distribution/app/Dockerfile \
  --build-arg FRONTEND_BRANCH=develop \
  -t wisemapping:develop .
```

Or use a specific tag:

```bash
docker build -f distribution/app/Dockerfile \
  --build-arg FRONTEND_BRANCH=v6.0.2 \
  -t wisemapping:6.0.2 .
```

## Production Recommendations

> **⚠️ CRITICAL**: Do **NOT** use HSQLDB for production. It is only suitable for development and testing.

1. **Use persistent volumes for data**
2. **Configure external database (PostgreSQL recommended, MySQL/MariaDB also supported)**
3. **Set up regular backups**
4. **Configure appropriate resource limits**
5. **Use HTTPS with proper SSL certificates**
6. **Set up monitoring and log aggregation**

## License

Copyright [2007-2025] [wisemapping]

Licensed under WiseMapping Public License, Version 1.0 (the "License").
See https://github.com/wisemapping/wisemapping-open-source/blob/main/LICENSE.md

