# WiseMapping with MariaDB

Docker Compose configuration for deploying WiseMapping with a MariaDB 11.4 database, using the native MariaDB JDBC driver and dialect (not the MySQL connector compatibility path).

## Prerequisites

- Docker and Docker Compose installed.
- `WISEMAPPING_DATA_DIR` set to a persistent storage path on your host.
- `MARIADB_ROOT_PASSWORD` set.

## Configuration Details

1. **Native MariaDB driver**: uses `org.mariadb.jdbc.Driver` and `org.hibernate.dialect.MariaDBDialect`.
2. **UTF8MB4 Support**: server is configured with `utf8mb4` character set / `utf8mb4_unicode_ci` collation.
3. **Case Sensitivity**: `--lower_case_table_names=1` matches WiseMapping's lowercase naming convention across Linux/Mac/Windows.
4. **Large Mindmaps**: schema uses `MEDIUMBLOB` for XML data and `MEDIUMTEXT` for descriptions.

## Quick Start

1. Create `.env` from `.env.example` and set the required values.
2. `docker-compose up -d`
3. Open `http://localhost` (or `http://localhost:${WISEMAPPING_PORT}`).
   - Default user: `test@wisemapping.org` / `test`
   - Admin user: `admin@wisemapping.org` / `testAdmin123`

## Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `WISEMAPPING_DATA_DIR` | Path to store database and logs | **Required** |
| `MARIADB_ROOT_PASSWORD` | Root password for MariaDB | **Required** |
| `MARIADB_DATABASE` | Name of the database | `wisemapping` |
| `MARIADB_USER` | Database user for WiseMapping | `wisemapping` |
| `MARIADB_PASSWORD` | Password for the database user | `password` |
| `WISEMAPPING_PORT` | Port to expose the application | `80` |
| `MARIADB_PORT` | Port to expose MariaDB | `3306` |
