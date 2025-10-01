# WiseMapping Docker Image

This Docker image contains the complete WiseMapping application with both frontend and backend services running on a single container.

## Quick Start

```bash
# Pull the image
docker pull your-dockerhub-username/wisemapping:latest

# Run with default configuration (HSQLDB)
docker run -p 80:80 your-dockerhub-username/wisemapping:latest
```

The application will be available at `http://localhost`

## Architecture

The image uses a multi-service architecture managed by Supervisor:

- **Nginx** (Port 80): Serves the frontend and proxies API requests
- **Spring Boot** (Port 8080): Backend API service
- **Database**: Configurable (HSQLDB, MySQL, PostgreSQL)

```
┌─────────────────────────────────────┐
│           Docker Container          │
│  ┌─────────────┐  ┌───────────────┐ │
│  │    Nginx    │  │  Spring Boot  │ │
│  │   (Port 80) │  │  (Port 8080)  │ │
│  └─────────────┘  └───────────────┘ │
│         │                │          │
│  Frontend + API Proxy    Backend    │
└─────────────────────────────────────┘
```

## Configuration

### Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `JAVA_OPTS` | `""` | JVM options for Spring Boot |
| `SPRING_PROFILES_ACTIVE` | `default` | Spring profiles to activate |

### Database Configuration

#### 1. HSQLDB (Default - In-Memory)
No additional configuration needed. Data is stored in memory and will be lost when container stops.

```bash
docker run -p 80:80 your-dockerhub-username/wisemapping:latest
```

#### 2. HSQLDB (File-Based - Persistent)
Mount a volume to persist data:

```bash
docker run -p 80:80 \
  -v wisemapping-data:/tmp \
  your-dockerhub-username/wisemapping:latest
```

#### 3. MySQL
Create a custom `app.yml` configuration file:

```yaml
# app.yml
spring:
  datasource:
    url: jdbc:mysql://your-mysql-host:3306/wisemapping
    username: your-username
    password: your-password
    driver-class-name: com.mysql.cj.jdbc.Driver
  jpa:
    database-platform: org.hibernate.dialect.MySQL8Dialect
```

```bash
# Create the config file
mkdir -p ./config
cat > ./config/app.yml << 'EOF'
spring:
  datasource:
    url: jdbc:mysql://your-mysql-host:3306/wisemapping
    username: your-username
    password: your-password
    driver-class-name: com.mysql.cj.jdbc.Driver
  jpa:
    database-platform: org.hibernate.dialect.MySQL8Dialect
EOF

# Run with MySQL configuration
docker run -p 80:80 \
  -v ./config:/app/config:ro \
  your-dockerhub-username/wisemapping:latest
```

#### 4. PostgreSQL
Create a custom `app.yml` configuration file:

```yaml
# app.yml
spring:
  datasource:
    url: jdbc:postgresql://your-postgres-host:5432/wisemapping
    username: your-username
    password: your-password
    driver-class-name: org.postgresql.Driver
  jpa:
    database-platform: org.hibernate.dialect.PostgreSQLDialect
```

```bash
# Create the config file
mkdir -p ./config
cat > ./config/app.yml << 'EOF'
spring:
  datasource:
    url: jdbc:postgresql://your-postgres-host:5432/wisemapping
    username: your-username
    password: your-password
    driver-class-name: org.postgresql.Driver
  jpa:
    database-platform: org.hibernate.dialect.PostgreSQLDialect
EOF

# Run with PostgreSQL configuration
docker run -p 80:80 \
  -v ./config:/app/config:ro \
  your-dockerhub-username/wisemapping:latest
```

## Docker Compose Examples

### Basic Setup with HSQLDB
```yaml
version: '3.8'
services:
  wisemapping:
    image: your-dockerhub-username/wisemapping:latest
    ports:
      - "80:80"
    volumes:
      - wisemapping-data:/tmp
volumes:
  wisemapping-data:
```

### With MySQL Database
```yaml
version: '3.8'
services:
  mysql:
    image: mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD: rootpassword
      MYSQL_DATABASE: wisemapping
      MYSQL_USER: wisemapping
      MYSQL_PASSWORD: wisemapping
    volumes:
      - mysql-data:/var/lib/mysql
    ports:
      - "3306:3306"

  wisemapping:
    image: your-dockerhub-username/wisemapping:latest
    ports:
      - "80:80"
    volumes:
      - ./config:/app/config:ro
    depends_on:
      - mysql
    environment:
      SPRING_PROFILES_ACTIVE: mysql

volumes:
  mysql-data:
```

### With PostgreSQL Database
```yaml
version: '3.8'
services:
  postgres:
    image: postgres:15
    environment:
      POSTGRES_DB: wisemapping
      POSTGRES_USER: wisemapping
      POSTGRES_PASSWORD: wisemapping
    volumes:
      - postgres-data:/var/lib/postgresql/data
    ports:
      - "5432:5432"

  wisemapping:
    image: your-dockerhub-username/wisemapping:latest
    ports:
      - "80:80"
    volumes:
      - ./config:/app/config:ro
    depends_on:
      - postgres
    environment:
      SPRING_PROFILES_ACTIVE: postgres

volumes:
  postgres-data:
```

## Advanced Configuration

### Custom Application Properties
You can override any Spring Boot property by creating an `app.yml` file:

```yaml
# app.yml
app:
  security:
    oauth2:
      google:
        enabled: true
        clientId: your-google-client-id
        clientSecret: your-google-client-secret
      facebook:
        enabled: true
        clientId: your-facebook-app-id
        clientSecret: your-facebook-app-secret
  registration:
    enabled: true
  api:
    http-basic-enabled: false

spring:
  datasource:
    url: jdbc:mysql://mysql:3306/wisemapping
    username: wisemapping
    password: wisemapping
  jpa:
    hibernate:
      ddl-auto: update
```

### JVM Tuning
```bash
docker run -p 80:80 \
  -e JAVA_OPTS="-Xmx2g -Xms1g -XX:+UseG1GC" \
  your-dockerhub-username/wisemapping:latest
```

### Health Check
The container includes a health check endpoint:

```bash
# Check if the application is healthy
curl http://localhost/health
```

## Troubleshooting

### View Logs
```bash
# View all logs
docker logs container-name

# Follow logs in real-time
docker logs -f container-name

# View specific service logs
docker exec container-name supervisorctl status
docker exec container-name supervisorctl tail -f spring-boot
docker exec container-name supervisorctl tail -f nginx
```

### Access Container Shell
```bash
docker exec -it container-name /bin/sh
```

### Check Service Status
```bash
docker exec container-name supervisorctl status
```

Expected output:
```
nginx                            RUNNING   pid 123, uptime 0:05:00
spring-boot                      RUNNING   pid 124, uptime 0:05:00
```

### Common Issues

1. **Port 80 already in use**
   ```bash
   # Use a different port
   docker run -p 8080:80 your-dockerhub-username/wisemapping:latest
   ```

2. **Database connection issues**
   - Check database is running and accessible
   - Verify connection parameters in `app.yml`
   - Check network connectivity between containers

3. **Frontend not loading**
   - Check Nginx logs: `docker exec container-name supervisorctl tail -f nginx`
   - Verify port mapping is correct

## Building from Source

If you want to build the image yourself:

```bash
# Clone the repository
git clone https://github.com/wisemapping/wisemapping-open-source.git
cd wisemapping-open-source

# Build the image
docker build -f distribution/app/Dockerfile -t wisemapping:latest .

# Run the built image
docker run -p 80:80 wisemapping:latest
```

## Security Considerations

1. **Database Credentials**: Never hardcode database credentials in Docker images. Use environment variables or mounted configuration files.

2. **OAuth Secrets**: Store OAuth client secrets securely and pass them via environment variables or configuration files.

3. **Network Security**: Consider using Docker networks to isolate database containers from external access.

4. **File Permissions**: The container runs as a non-root user (`wisemapping:1001`) for security.

## Support

For issues and questions:
- GitHub Issues: [wisemapping-open-source](https://github.com/wisemapping/wisemapping-open-source/issues)
- Documentation: [WiseMapping Docs](https://github.com/wisemapping/wisemapping-open-source/blob/main/README.md)

## License

This project is licensed under the AGPL-3.0 License - see the [LICENSE](https://github.com/wisemapping/wisemapping-open-source/blob/main/LICENSE.md) file for details.
