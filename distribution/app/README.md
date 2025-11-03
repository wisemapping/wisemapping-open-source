# WiseMapping 

[![Docker Pulls](https://img.shields.io/docker/pulls/wisemapping/wisemapping.svg)](https://hub.docker.com/r/wisemapping/wisemapping)
[![Docker Stars](https://img.shields.io/docker/stars/wisemapping/wisemapping.svg)](https://hub.docker.com/r/wisemapping/wisemapping)

**WiseMapping** is an open-source, web-based mind mapping application for organizing ideas, planning projects, and visualizing information. Self-hosted and privacy-focused, it gives you complete control over your data.

- **Website**: [https://www.wisemapping.com](https://www.wisemapping.com)
- **GitHub**: [https://github.com/wisemapping/wisemapping-open-source](https://github.com/wisemapping/wisemapping-open-source)

## Quick Start

### Simplest Start (Testing Only)

```bash
docker run -d --name wisemapping -p 80:80 wisemapping/wisemapping:latest
```

Access at http://localhost (data will be lost when container stops)

### Run with Persistent Data

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

# Run with persistent storage
docker run -d --name wisemapping -p 80:80 \
  -v wisemapping-db:/var/lib/wisemapping \
  -v $(pwd)/app.yml:/app/config/app.yml:ro \
  wisemapping/wisemapping:latest
```

**Default Credentials:**
- Test user: `test@wisemapping.org` / `password`
- Admin user: `admin@wisemapping.org` / `testAdmin123`

## Usage

### Port Configuration

By default, WiseMapping listens on container port `80`. Map it to any host port:

```bash
# Use port 8080
docker run -d --name wisemapping -p 8080:80 wisemapping/wisemapping:latest

# Use port 3000
docker run -d --name wisemapping -p 3000:80 wisemapping/wisemapping:latest

# Bind to specific IP
docker run -d --name wisemapping -p 127.0.0.1:8081:80 wisemapping/wisemapping:latest
```

The application automatically detects the port from requests and adjusts URLs accordingly.

### Database Configuration

> **⚠️ Production Warning**: HSQLDB is **NOT recommended for production**. Use PostgreSQL or MySQL/MariaDB for production deployments.

#### HSQLDB (Default - Testing Only)

The default configuration uses in-memory HSQLDB. Data is lost when the container stops:

```bash
docker run -d --name wisemapping -p 80:80 wisemapping/wisemapping:latest
```

For persistent HSQLDB storage (testing/small deployments only), mount a volume:

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

```bash
docker run -d --name wisemapping -p 80:80 \
  -v wisemapping-db:/var/lib/wisemapping \
  -v $(pwd)/app.yml:/app/config/app.yml:ro \
  wisemapping/wisemapping:latest
```

#### PostgreSQL (Production Recommended)

```yaml
# app.yml
spring:
  datasource:
    url: jdbc:postgresql://your-postgres-host:5432/wisemapping
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

```bash
docker run -d --name wisemapping -p 80:80 \
  -v $(pwd)/app.yml:/app/config/app.yml:ro \
  wisemapping/wisemapping:latest
```

#### MySQL/MariaDB

```yaml
# app.yml
spring:
  datasource:
    url: jdbc:mysql://your-mysql-host:3306/wisemapping?useUnicode=true&characterEncoding=utf8
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

### Environment Variables

Configure Java memory options:

```bash
docker run -d --name wisemapping -p 80:80 \
  -e JAVA_OPTS="-Xmx2048m -Xms1024m" \
  wisemapping/wisemapping:latest
```

### Container Paths

- **Configuration**: `/app/config/app.yml` (mount your config file here)
- **HSQLDB Database**: `/var/lib/wisemapping/db/` (mount volume here for persistent storage)
- **Application User**: `wisemapping` (UID 1001)

### Access Points

- **Frontend UI**: `http://localhost` (or your configured port)
- **API**: `http://localhost/api`
- **Health Check**: `http://localhost/health`

## Advanced Configuration

### Custom Application Properties

Mount a custom `app.yml` configuration file:

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

```bash
docker run -d --name wisemapping -p 80:80 \
  -v $(pwd)/app.yml:/app/config/app.yml:ro \
  wisemapping/wisemapping:latest
```

### SSL/HTTPS with Reverse Proxy

Place WiseMapping behind a reverse proxy (Traefik, Nginx, etc.) for SSL/HTTPS termination. Ensure the reverse proxy forwards the original `Host` header (including port) to the container.

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

### Common Issues

**Port already in use:**
```bash
docker run -d --name wisemapping -p 8080:80 wisemapping/wisemapping:latest
```

**Permission issues (host directory):**
```bash
sudo chown -R 1001:1001 /path/to/wisemapping-data
```

**Database connection issues:**
- Verify external database is running and accessible
- Check credentials in `app.yml`
- Ensure network connectivity between containers

### Backup and Restore

**Backup HSQLDB data:**
```bash
docker run --rm \
  -v wisemapping-db:/data \
  -v $(pwd):/backup \
  alpine tar czf /backup/wisemapping-backup-$(date +%Y%m%d).tar.gz -C /data .
```

**Restore HSQLDB data:**
```bash
docker run --rm \
  -v wisemapping-db:/data \
  -v $(pwd):/backup \
  alpine sh -c "cd /data && tar xzf /backup/wisemapping-backup-YYYYMMDD.tar.gz"
```

## Production Recommendations

> **⚠️ Important**: Do **NOT** use HSQLDB for production deployments.

1. **Use an external database** (PostgreSQL recommended, MySQL/MariaDB also supported)
2. **Set up regular backups**
3. **Configure resource limits**
4. **Use HTTPS** with reverse proxy
5. **Set up monitoring**
6. **Change default admin credentials**

## Support & Documentation

- **GitHub Repository**: [https://github.com/wisemapping/wisemapping-open-source](https://github.com/wisemapping/wisemapping-open-source)
- **Issue Tracker**: [https://github.com/wisemapping/wisemapping-open-source/issues](https://github.com/wisemapping/wisemapping-open-source/issues)
- **Full Documentation**: [https://github.com/wisemapping/wisemapping-open-source/tree/main/distribution/app](https://github.com/wisemapping/wisemapping-open-source/tree/main/distribution/app)

## License

Licensed under WiseMapping Public License, Version 1.0.

Copyright [2007-2025] [wisemapping]

See [LICENSE.md](https://github.com/wisemapping/wisemapping-open-source/blob/main/LICENSE.md) for details.
