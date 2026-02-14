# WiseMapping with PostgreSQL - Docker Compose

This directory contains a **production-ready** Docker Compose configuration for running WiseMapping with PostgreSQL as the database backend.

## 📁 Directory Structure

```
distribution/app-postgresql/
├── docker-compose.yml    # Main Docker Compose configuration
├── app.yml              # WiseMapping application configuration
├── .env                 # Environment variables (create from .env.example)
├── .env.example         # Environment variables template
├── .gitignore           # Git ignore rules
├── QUICKSTART.md        # Quick start guide
└── README.md            # This file
```

## 🚀 Quick Start

### Prerequisites
- Docker Engine 20.10+
- Docker Compose v2.0+
- At least 4GB RAM available for containers

### Step 1: Navigate to Directory

```bash
cd distribution/app-postgresql
```

### Step 2: Configure Storage Path (REQUIRED)

You **must** specify where PostgreSQL data will be stored. Choose one of these options:

**Option A: Edit the .env file (Recommended)**

```bash
# Edit .env file and set WISEMAPPING_DATA_DIR
nano .env

# Set to your desired storage path, for example:
# WISEMAPPING_DATA_DIR=/var/lib/wisemapping/data
# or
# WISEMAPPING_DATA_DIR=/home/user/wisemapping-data
```

**Option B: Set environment variable directly**

```bash
export WISEMAPPING_DATA_DIR=/path/to/your/storage
```

**Option C: Inline with docker-compose command**

```bash
WISEMAPPING_DATA_DIR=/path/to/your/storage docker-compose up -d
```

⚠️ **Important**: 
- The directory must exist before starting
- The directory must be writable by UID 999 (PostgreSQL user in the container)

**Create and set permissions:**

```bash
# Create the main data directory
mkdir -p /path/to/your/storage

# Create subdirectories for PostgreSQL and logs
mkdir -p /path/to/your/storage/logs
mkdir -p /path/to/your/storage/db

# Set proper ownership
# PostgreSQL runs as UID 999, WiseMapping app runs as UID 1001
sudo chown -R 999:999 /path/to/your/storage/db
sudo chown -R 1001:1001 /path/to/your/storage/logs

# Or make it world-writable (simpler but less secure)
chmod -R 777 /path/to/your/storage
```

### Step 3: Start the Application

```bash
# Start all services in detached mode
docker-compose up -d
```

This will:
1. 📦 Pull the PostgreSQL 15 Alpine image (~80MB)
2. 📦 Pull the WiseMapping latest image
3. 🗄️ Create a PostgreSQL database named `wisemapping`
4. 🔧 Initialize the database schema automatically
5. 🚀 Start the WiseMapping application
6. ✅ Make the application available at http://localhost

### Step 3: Access the Application

- **Frontend**: http://localhost
- **API**: http://localhost/api
- **Health Check**: http://localhost/health

**Default Test Credentials:**
- Email: `test@wisemapping.org`
- Password: `password`

**Default Admin Credentials:**
- Email: `admin@wisemapping.org`
- Password: `testAdmin123`

### Step 4: Verify Setup

```bash
# Check if containers are running
docker-compose ps

# Check logs
docker-compose logs -f
```

### Step 5: Stop the Application

```bash
# Stop all services
docker-compose down
```

⚠️ **Note**: Your data is preserved in the directory you specified (`WISEMAPPING_DATA_DIR`). The data will persist even after stopping the containers.

## 📋 What Gets Created?

When you run `docker-compose up`, the following resources are created:

### Containers
- `wisemapping-postgresql` - PostgreSQL 15 database server
- `wisemapping-app` - WiseMapping application (frontend + backend)

### Data Storage
- **PostgreSQL Data**: Stored in `${WISEMAPPING_DATA_DIR}/db/` (subdirectory)
  - This is a **bind mount** to your host filesystem
  - Data persists independently of containers
  - Easy to backup and manage
- **Application Logs**: Stored in `${WISEMAPPING_DATA_DIR}/logs/`
  - Spring Boot application logs
  - Nginx access and error logs
  - Supervisor process logs
  - Accessible directly from your host filesystem

### Network
- `wisemapping-network` - Bridge network for container communication

### Ports Exposed
- `${WISEMAPPING_PORT}` (default: 80) - WiseMapping application (HTTP)
- `${POSTGRES_PORT}` (default: 5432) - PostgreSQL database (for external access/backups)

## 📋 Configuration

### Storage Configuration (REQUIRED)

**Environment Variables:**

| Variable | Required | Default | Description |
|----------|----------|---------|-------------|
| `WISEMAPPING_DATA_DIR` | **YES** | - | Absolute path for PostgreSQL data storage |
| `POSTGRES_PASSWORD` | **YES** | - | PostgreSQL password |
| `POSTGRES_DB` | No | `wisemapping` | Database name |
| `POSTGRES_USER` | No | `wisemapping` | Database username |
| `WISEMAPPING_PORT` | No | `80` | Application HTTP port |
| `POSTGRES_PORT` | No | `5432` | PostgreSQL port |
| `JAVA_OPTS` | No | `-Xmx2048m -Xms1024m` | JVM options |

### Database Configuration

The PostgreSQL database is configured with:
- **Database**: `wisemapping` (configurable via `POSTGRES_DB`)
- **Username**: `wisemapping` (configurable via `POSTGRES_USER`)
- **Password**: Set via `POSTGRES_PASSWORD` (⚠️ **Required - set a secure password!**)
- **Port**: `5432` (configurable via `POSTGRES_PORT`)
- **Data Storage**: Set via `WISEMAPPING_DATA_DIR` (⚠️ **Required**)

### Application Configuration

The WiseMapping application is configured via `app.yml`:
- Uses PostgreSQL as the database
- Hibernate validation mode (no auto DDL changes)
- Connection pool optimized for PostgreSQL

### Default Credentials

- **Test User**: test@wisemapping.org / password
- **Admin User**: admin@wisemapping.org / testAdmin123

## 🔧 Customization

### Change Database Password

1. Edit `docker-compose.yml`:
```yaml
environment:
  POSTGRES_PASSWORD: your_secure_password
```

2. Edit `app.yml`:
```yaml
spring:
  datasource:
    password: your_secure_password
```

### Increase Java Memory

Edit `docker-compose.yml`:
```yaml
environment:
  - JAVA_OPTS=-Xmx4096m -Xms2048m
```

### Use Different PostgreSQL Version

Edit `docker-compose.yml`:
```yaml
postgres:
  image: postgres:16-alpine
```

### Enable Mail Configuration

Add to `app.yml`:
```yaml
app:
  mail:
    enabled: true
    host: smtp.gmail.com
    port: 587
    username: your_email@gmail.com
    password: your_app_password
    sender-email: noreply@wisemapping.com
```

### Configure Google OAuth

Add to `app.yml`:
```yaml
app:
  security:
    oauth2:
      google:
        enabled: true
        clientId: YOUR_CLIENT_ID
        clientSecret: YOUR_CLIENT_SECRET
```

## 📊 Database Management

### Access PostgreSQL Shell

```bash
docker exec -it wisemapping-postgresql psql -U wisemapping -d wisemapping
```

### Backup Database

**Method 1: Using pg_dump (SQL format)**
```bash
docker exec wisemapping-postgresql pg_dump -U wisemapping wisemapping > backup-$(date +%Y%m%d).sql
```

**Method 2: Direct filesystem backup (requires stopping the database)**
```bash
# Stop the containers
docker-compose down

# Copy the entire data directory
cp -a ${WISEMAPPING_DATA_DIR} ${WISEMAPPING_DATA_DIR}-backup-$(date +%Y%m%d)

# Restart
docker-compose up -d
```

**Method 3: Using tar while running**
```bash
docker exec wisemapping-postgresql pg_basebackup -U wisemapping -D /tmp/backup -Ft
docker cp wisemapping-postgresql:/tmp/backup ./backup-$(date +%Y%m%d).tar
```

### Restore Database

**From SQL dump:**
```bash
docker exec -i wisemapping-postgresql psql -U wisemapping wisemapping < backup-20241013.sql
```

**From filesystem backup:**
```bash
# Stop containers
docker-compose down

# Replace data directory
rm -rf ${WISEMAPPING_DATA_DIR}/*
cp -a ${WISEMAPPING_DATA_DIR}-backup-20241013/* ${WISEMAPPING_DATA_DIR}/

# Restart
docker-compose up -d
```

### View Database Logs

```bash
docker logs wisemapping-postgresql
```

## 🔍 Monitoring & Logs

### View Application Logs

**Method 1: Via Docker logs command**
```bash
# Follow logs
docker logs -f wisemapping-app

# Last 100 lines
docker logs --tail 100 wisemapping-app
```

**Method 2: Direct filesystem access (recommended)**
```bash
# All logs are in ${WISEMAPPING_DATA_DIR}/logs/

# Spring Boot application logs
tail -f ${WISEMAPPING_DATA_DIR}/logs/spring-boot-stdout*.log

# Nginx access logs
tail -f ${WISEMAPPING_DATA_DIR}/logs/nginx-access.log

# Nginx error logs
tail -f ${WISEMAPPING_DATA_DIR}/logs/nginx-error.log

# Supervisor logs
tail -f ${WISEMAPPING_DATA_DIR}/logs/supervisord.log
```

**Method 3: Access logs inside container**
```bash
# Get a shell in the container
docker exec -it wisemapping-app /bin/sh

# Logs are in /var/log/supervisor/
ls -la /var/log/supervisor/
```

### Check Container Status

```bash
docker-compose ps
```

### Check Database Health

```bash
docker exec wisemapping-postgresql pg_isready -U wisemapping
```

## 🐛 Troubleshooting

### Database Connection Issues

1. Check if PostgreSQL is ready:
```bash
docker-compose logs postgres
```

2. Verify network connectivity:
```bash
docker exec wisemapping-app ping postgres
```

3. Check database credentials in `app.yml`

### Application Won't Start

1. Check Java memory settings:
```bash
docker logs wisemapping-app | grep -i "memory\|heap"
```

2. Verify database schema:
```bash
docker exec -it wisemapping-postgresql psql -U wisemapping -d wisemapping -c "\dt"
```

### Port Already in Use

Change the port mapping in `docker-compose.yml`:
```yaml
ports:
  - "8080:80"  # Access at http://localhost:8080
```

### Database Migration Issues

If you're upgrading from a previous version and encounter schema issues:

1. Check migration scripts:
```bash
# Run appropriate migration
docker exec -i wisemapping-postgresql psql -U wisemapping wisemapping < ../../wise-api/src/main/resources/migration-6.5->6.6-postgresql.sql
```

## 🚀 Production Deployment

### Recommendations

1. **Change default passwords** in both `docker-compose.yml` and `app.yml`
2. **Use Docker secrets** for sensitive data
3. **Enable SSL/TLS** with a reverse proxy (Nginx, Traefik)
4. **Set up regular backups**
5. **Configure resource limits**:

```yaml
services:
  wisemapping:
    deploy:
      resources:
        limits:
          cpus: '2'
          memory: 4G
        reservations:
          cpus: '1'
          memory: 2G
```

6. **Use external volumes** for PostgreSQL data:

```yaml
volumes:
  postgres_data:
    driver: local
    driver_opts:
      type: none
      o: bind
      device: /path/to/postgres/data
```

### HTTPS Setup with Traefik

Example `docker-compose.override.yml`:

```yaml
version: '3.8'

services:
  traefik:
    image: traefik:v2.10
    ports:
      - "80:80"
      - "443:443"
    volumes:
      - /var/run/docker.sock:/var/run/docker.sock:ro
      - ./traefik.yml:/traefik.yml:ro
      - ./acme.json:/acme.json
    networks:
      - wisemapping-network

  wisemapping:
    labels:
      - "traefik.enable=true"
      - "traefik.http.routers.wisemapping.rule=Host(`your-domain.com`)"
      - "traefik.http.routers.wisemapping.tls=true"
      - "traefik.http.routers.wisemapping.tls.certresolver=letsencrypt"
    ports: []  # Remove direct port exposure
```

## 📚 Additional Resources

- [PostgreSQL Documentation](https://www.postgresql.org/docs/)
- [WiseMapping Documentation](https://github.com/wisemapping/wisemapping-open-source)
- [Docker Compose Documentation](https://docs.docker.com/compose/)

## 📝 Version Information

- PostgreSQL: 15-alpine
- WiseMapping: latest
- Docker Compose: 3.8

## 🆘 Support

For issues and questions:
- GitHub Issues: https://github.com/wisemapping/wisemapping-open-source/issues
- Documentation: https://github.com/wisemapping/wisemapping-open-source

