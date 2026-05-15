# WiseMapping with MySQL/MariaDB

This directory contains a Docker Compose configuration for deploying WiseMapping with a MySQL 8.0 database.

## Prerequisites

- Docker and Docker Compose installed.
- Set the `WISEMAPPING_DATA_DIR` environment variable to a persistent storage path on your host.
- Set the `MYSQL_ROOT_PASSWORD` environment variable.

## Configuration Details

This configuration addresses common issues reported when using MySQL/MariaDB with WiseMapping (specifically related to [Issue #69](https://github.com/wisemapping/wisemapping-open-source/issues/69)):

1.  **UTF8MB4 Support**: The database is configured to use `utf8mb4` encoding to support a wider range of characters (including emojis).
2.  **Case Sensitivity**: The MySQL container is started with `--lower_case_table_names=1` to ensure compatibility with WiseMapping's table naming conventions across different operating systems.
3.  **Large Mindmaps**: The schema uses `MEDIUMBLOB` for XML data and `MEDIUMTEXT` for descriptions to accommodate large mindmaps.

## Quick Start

1. Create a `.env` file based on `.env.example` (if provided) or set the required environment variables:
   ```bash
   export WISEMAPPING_DATA_DIR=/path/to/your/data
   export MYSQL_ROOT_PASSWORD=your_secure_password
   ```

2. Start the services:
   ```bash
   docker-compose up -d
   ```

3. Access the application:
   - URL: `http://localhost` (or your configured port)
   - Default User: `test@wisemapping.org` / `test`
   - Admin User: `admin@wisemapping.org` / `testAdmin123`

## Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `WISEMAPPING_DATA_DIR` | Path to store database and logs | **Required** |
| `MYSQL_ROOT_PASSWORD` | Root password for MySQL | **Required** |
| `MYSQL_DATABASE` | Name of the database | `wisemapping` |
| `MYSQL_USER` | Database user for WiseMapping | `wisemapping` |
| `MYSQL_PASSWORD` | Password for the database user | `password` |
| `WISEMAPPING_PORT` | Port to expose the application | `80` |
| `MYSQL_PORT` | Port to expose MySQL | `3306` |

## Troubleshooting

### Encoding Issues
If you encounter encoding issues, ensure your JDBC connection string in `app.yml` includes `useUnicode=true&characterEncoding=utf8mb4`.

### Table Name Case Sensitivity
On Linux, MySQL table names are case-sensitive by default. This configuration uses `--lower_case_table_names=1` to avoid issues where the application might expect different casing than what is stored in the database.
