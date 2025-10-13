# WiseMapping Docker - Complete User Guide

[![Docker Pulls](https://img.shields.io/docker/pulls/wisemapping/wisemapping.svg)](https://hub.docker.com/r/wisemapping/wisemapping)
[![Docker Stars](https://img.shields.io/docker/stars/wisemapping/wisemapping.svg)](https://hub.docker.com/r/wisemapping/wisemapping)

## About WiseMapping

**WiseMapping** is a powerful, web-based mind mapping application that helps you organize ideas, plan projects, and visualize complex information through intuitive visual diagrams. Whether you're brainstorming, taking notes, or structuring thoughts, WiseMapping provides an easy-to-use interface for creating and sharing mind maps.

### 🌟 Key Features

- **🖥️ Modern Web Interface**: Clean, responsive design that works on desktop, tablet, and mobile devices
- **🎨 Rich Visual Elements**: Support for icons, colors, fonts, and custom styling to make your maps visually appealing
- **👥 Real-time Collaboration**: Share mind maps with team members and collaborate in real-time
- **📱 Multi-platform Access**: Access your maps from any device with a web browser
- **🔒 Privacy & Security**: Self-hosted solution gives you complete control over your data
- **📊 Export Options**: Export your mind maps to various formats (PDF, PNG, SVG, etc.)
- **🔍 Search & Navigation**: Quickly find content across all your mind maps
- **📝 Rich Text Support**: Add detailed notes, links, and formatted text to your nodes
- **🌐 Multi-language Support**: Available in multiple languages including English, Spanish, French, German, Italian, Russian, and Chinese

### 🎯 Perfect For

- **Business Planning**: Strategy sessions, project roadmaps, and organizational charts
- **Education**: Study guides, lesson planning, and knowledge organization
- **Personal Use**: Goal setting, habit tracking, and personal project management
- **Team Collaboration**: Meeting notes, brainstorming sessions, and workflow documentation
- **Research**: Information gathering, analysis, and knowledge mapping

### 🌐 Try WiseMapping

- **Live Demo**: [https://app.wisemapping.com](https://app.wisemapping.com)
- **Official Website**: [https://www.wisemapping.com](https://www.wisemapping.com)
- **Documentation**: [https://github.com/wisemapping/wisemapping-open-source](https://github.com/wisemapping/wisemapping-open-source)

---

## Docker Deployment

WiseMapping is now available as a complete full-stack Docker image that includes both the backend API and frontend UI in a single container. This self-contained solution makes deployment simple and secure, giving you complete control over your mind mapping data and infrastructure.

### 🚀 Why Use Docker?

- **⚡ Quick Setup**: Get WiseMapping running in minutes with a single command
- **🔒 Data Control**: Keep your mind maps private and secure on your own infrastructure
- **📦 Self-contained**: No complex dependencies or configuration required
- **🔄 Easy Updates**: Update to the latest version with a simple docker pull
- **🌍 Flexible Deployment**: Run on any platform that supports Docker (cloud, on-premises, local development)

## Quick Start

### 1. Pull the Latest Image
```bash
docker pull wisemapping/wisemapping:latest
```

### 2. Run with Persistent Data (Recommended)
```bash
# Create configuration file
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
  wisemapping/wisemapping:latest
```

### 3. Access Your Application
- **Frontend**: http://localhost
- **API**: http://localhost/api
- **Health Check**: http://localhost/health

## Database Configuration

> **⚠️ PRODUCTION WARNING**: HSQLDB is **NOT recommended for production environments**. It's designed for testing, development, and small-scale deployments only. For production use, please configure an external database like **PostgreSQL** (recommended) or **MySQL/MariaDB**.

### HSQLDB (Default - In-Memory)
```bash
# Quick test (data will be lost when container stops)
docker run -d --name wisemapping -p 80:80 wisemapping/wisemapping:latest
```

### HSQLDB (Persistent File-Based)

> **⚠️ WARNING**: Only use this configuration for **testing** or **small personal deployments**. Not suitable for production.

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

**Mount directory**: `/var/lib/wisemapping/db/` (inside container)
**Docker volume**: `wisemapping-db`

### PostgreSQL
```yaml
# app.yml
spring:
  datasource:
    url: jdbc:postgresql://postgres:5432/wisemapping
    username: wisemapping
    password: your_password
    driver-class-name: org.postgresql.Driver
  jpa:
    hibernate:
      ddl-auto: validate
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
  sql:
    init:
      platform: postgresql
```

### MySQL/MariaDB
```yaml
# app.yml
spring:
  datasource:
    url: jdbc:mysql://mysql:3306/wisemapping?useUnicode=true&characterEncoding=utf8
    username: wisemapping
    password: your_password
    driver-class-name: com.mysql.cj.jdbc.Driver
  jpa:
    hibernate:
      ddl-auto: validate
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQLDialect
  sql:
    init:
      platform: mysql
```

## Complete Examples

### Example 1: HSQLDB with Docker Volume
```bash
# Create config
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

# Run with persistent storage
docker run -d \
  --name wisemapping \
  -p 80:80 \
  -v wisemapping-db:/var/lib/wisemapping \
  -v $(pwd)/app.yml:/app/config/app.yml:ro \
  wisemapping/wisemapping:latest
```

### Example 2: HSQLDB with Host Directory
```bash
# Create host directory
mkdir -p /path/to/wisemapping-data

# Create config pointing to mounted path
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
  wisemapping/wisemapping:latest
```

### Example 3: PostgreSQL with Docker Compose
```yaml
# docker-compose.yml
version: '3.8'
services:
  postgres:
    image: postgres:15
    environment:
      POSTGRES_DB: wisemapping
      POSTGRES_USER: wisemapping
      POSTGRES_PASSWORD: password
    volumes:
      - postgres_data:/var/lib/postgresql/data
    ports:
      - "5432:5432"

  wisemapping:
    image: wisemapping/wisemapping:latest
    ports:
      - "80:80"
    volumes:
      - ./app.yml:/app/config/app.yml:ro
    depends_on:
      - postgres

volumes:
  postgres_data:
```

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
  sql:
    init:
      platform: postgresql
```

## Important Paths & Ports

### Container Paths
- **Configuration**: `/app/config/app.yml` (mount your config here)
- **HSQLDB Database**: `/var/lib/wisemapping/db/` (for persistent storage)
- **Application User**: `wisemapping` (UID 1001)
- **Logs**: Available via `docker logs wisemapping`

### Ports
- **Port 80**: All traffic (Nginx routes to frontend or backend)
  - Frontend: `http://localhost`
  - API: `http://localhost/api`
  - Health: `http://localhost/health`

## Environment Variables

### Java Memory Configuration
```bash
docker run -d \
  --name wisemapping \
  -p 80:80 \
  -e JAVA_OPTS="-Xmx2048m -Xms1024m" \
  wisemapping/wisemapping:latest
```

## Data Management

### Backup HSQLDB Data
```bash
# Stop container
docker stop wisemapping

# Backup data
docker run --rm \
  -v wisemapping-db:/data \
  -v $(pwd):/backup \
  alpine tar czf /backup/wisemapping-backup-$(date +%Y%m%d).tar.gz -C /data .

# Restart container
docker start wisemapping
```

### Restore HSQLDB Data
```bash
# Stop and remove container
docker stop wisemapping && docker rm wisemapping

# Restore data
docker run --rm \
  -v wisemapping-db:/data \
  -v $(pwd):/backup \
  alpine sh -c "cd /data && tar xzf /backup/wisemapping-backup-YYYYMMDD.tar.gz"

# Start container again
docker run -d \
  --name wisemapping \
  -p 80:80 \
  -v wisemapping-db:/var/lib/wisemapping \
  -v $(pwd)/app.yml:/app/config/app.yml:ro \
  wisemapping/wisemapping:latest
```

## Troubleshooting

### View Logs
```bash
# All logs
docker logs -f wisemapping

# Backend logs only
docker exec wisemapping tail -f /var/log/supervisor/spring-boot-stdout---supervisor-*.log

# Nginx logs
docker exec wisemapping tail -f /var/log/nginx/access.log
```

### Port Already in Use
```bash
# Use different port
docker run -d --name wisemapping -p 8080:80 wisemapping/wisemapping:latest
```

### Permission Issues
```bash
# Fix ownership for host directory
sudo chown -R 1001:1001 /path/to/wisemapping-data
```

### Database Connection Issues
- Ensure external database is running and accessible
- Check credentials in your `app.yml` configuration
- Verify network connectivity between containers
- For HSQLDB: Ensure the volume is properly mounted and writable

## Production Recommendations

> **⚠️ CRITICAL**: Do **NOT** use HSQLDB for production deployments. HSQLDB is suitable only for development, testing, and small-scale personal use.

1. **Use an external database** (PostgreSQL recommended, MySQL/MariaDB also supported)
2. **Set up regular backups**
3. **Configure resource limits**
4. **Use HTTPS** with reverse proxy
5. **Set up monitoring**
6. **Change default admin credentials**

## Architecture

The Docker image uses a single-port design:
- **Nginx** (port 80) handles all requests
- Routes `/api/*` to Spring Boot backend (internal port 8080)
- Serves frontend static files for all other requests
- **Supervisor** manages both Nginx and Spring Boot processes

This design eliminates CORS issues and simplifies deployment by requiring only one exposed port.

## Advanced Configuration

### Custom Application Properties
```yaml
# app.yml
app:
  admin:
    user: admin@wisemapping.com
  mail:
    sender-email: noreply@wisemapping.com
    enabled: true
    host: smtp.example.com
    port: 587
    username: your_email@example.com
    password: your_password
  security:
    oauth2:
      google:
        enabled: true
        clientId: YOUR_CLIENT_ID
        clientSecret: YOUR_CLIENT_SECRET
  site:
    ui-base-url: https://your-domain.com
    api-base-url: https://your-domain.com
```

### SSL/HTTPS with Reverse Proxy
```yaml
# docker-compose.yml with Traefik
version: '3.8'
services:
  wisemapping:
    image: wisemapping/wisemapping:latest
    labels:
      - "traefik.enable=true"
      - "traefik.http.routers.wisemapping.rule=Host(`your-domain.com`)"
      - "traefik.http.routers.wisemapping.tls=true"
      - "traefik.http.routers.wisemapping.tls.certresolver=letsencrypt"
      - "traefik.http.services.wisemapping.loadbalancer.server.port=80"
    volumes:
      - ./app.yml:/app/config/app.yml:ro
```

## Building from Source

If you want to build the Docker image from source:

```bash
# Clone the repository
git clone https://github.com/wisemapping/wisemapping-open-source.git
cd wisemapping-open-source

# Build with default frontend branch (main)
docker build -f distribution/app/Dockerfile -t wisemapping:latest .

# Build with specific frontend branch
docker build -f distribution/app/Dockerfile \
  --build-arg FRONTEND_BRANCH=develop \
  --build-arg CACHEBUST=$(date +%s) \
  -t wisemapping:develop .
```

## Support

- **GitHub Repository**: https://github.com/wisemapping/wisemapping-open-source
- **Issues**: https://github.com/wisemapping/wisemapping-open-source/issues
- **Documentation**: https://github.com/wisemapping/wisemapping-open-source/tree/main/distribution/app

## License

Copyright [2007-2025] [wisemapping]

Licensed under WiseMapping Public License, Version 1.0 (the "License").
See https://github.com/wisemapping/wisemapping-open-source/blob/main/LICENSE.md
