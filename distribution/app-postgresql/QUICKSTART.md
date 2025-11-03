# Quick Start Guide - WiseMapping with PostgreSQL

## 1. Clone or navigate to the directory

```bash
cd distribution/app-postgresql
```

## 2. Set your data storage path

**Choose where you want to store your PostgreSQL data:**

```bash
# Example 1: Store in /var/lib/wisemapping/data
export WISEMAPPING_DATA_DIR=/var/lib/wisemapping/data

# Example 2: Store in your home directory
export WISEMAPPING_DATA_DIR=$HOME/wisemapping-data

# Example 3: Store locally in this directory
export WISEMAPPING_DATA_DIR=$(pwd)/data
```

## 3. Create and prepare the directory

```bash
# Create the main directory and subdirectories
mkdir -p ${WISEMAPPING_DATA_DIR}/logs

# Set ownership
# PostgreSQL data: UID 999
# Application logs: UID 1001
sudo chown -R 999:999 ${WISEMAPPING_DATA_DIR}
sudo chown -R 1001:1001 ${WISEMAPPING_DATA_DIR}/logs

# OR make it world-writable (simpler but less secure)
chmod -R 777 ${WISEMAPPING_DATA_DIR}
```

**What gets stored:**
- `${WISEMAPPING_DATA_DIR}/` - PostgreSQL database files
- `${WISEMAPPING_DATA_DIR}/logs/` - Application and web server logs

## 4. Set PostgreSQL password (optional but recommended)

```bash
export POSTGRES_PASSWORD=your_secure_password
```

## 5. Start WiseMapping

```bash
docker-compose up -d
```

## 6. Access the application

Open your browser and navigate to:

### 🌐 Application URL
- **Default**: http://localhost
- **Custom port**: http://localhost:${WISEMAPPING_PORT}
- **API Endpoint**: http://localhost/api
- **Health Check**: http://localhost/health

### 🔑 Login Credentials

**Test User:**
- Email: `test@wisemapping.org`
- Password: `password`

**Admin User:**
- Email: `admin@wisemapping.org`
- Password: `testAdmin123`

⚠️ **Important**: Change these default passwords in production!

## 7. Check status

```bash
# View running containers
docker-compose ps

# View logs
docker-compose logs -f
```

## 8. Stop when done

```bash
docker-compose down
```

Your data remains in `${WISEMAPPING_DATA_DIR}` even after stopping!

---

## Alternative: Using .env file

Instead of exporting variables, you can edit the `.env` file:

```bash
# Edit .env
nano .env

# Set your paths:
WISEMAPPING_DATA_DIR=/var/lib/wisemapping/data
POSTGRES_PASSWORD=your_secure_password
```

Then just run:

```bash
docker-compose up -d
```

---

## What if I forget to set WISEMAPPING_DATA_DIR?

Docker Compose will show an error:

```
ERROR: WISEMAPPING_DATA_DIR is required - please set the storage path
```

This is intentional to ensure you explicitly choose where your data is stored!


---

## Accessing Logs

Once running, you can access logs directly from your filesystem:

```bash
# View Spring Boot logs
tail -f ${WISEMAPPING_DATA_DIR}/logs/spring-boot-stdout*.log

# View Nginx access logs
tail -f ${WISEMAPPING_DATA_DIR}/logs/nginx-access.log

# View all logs
ls -lh ${WISEMAPPING_DATA_DIR}/logs/
```

This makes troubleshooting easy without needing to use `docker logs` or exec into containers!
