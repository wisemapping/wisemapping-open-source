# WiseMapping Open Source

WiseMapping is a free, open-source, web-based mind mapping tool designed for individuals, teams, and educational institutions. It enables users to create, share, and collaborate on mind maps in real-time, facilitating brainstorming sessions, project planning, and knowledge management. Built with modern open standards technologies like SVG and React, WiseMapping provides a versatile and user-friendly platform to visualize and organize complex information effectively. The open-source codebase powers https://www.wisemapping.com, ensuring reliability and continuity in its development.

## 🎯 Capabilities

WiseMapping provides a comprehensive set of features for creating, managing, and sharing mind maps:

- **🎨 Visual Mapping**: Create rich mind maps with icons, colors, fonts, and custom styling
- **👥 Collaboration**: Share mind maps with team members and collaborate in real-time
- **📱 Multi-platform**: Access your maps from any device with a modern web browser
- **📊 Export & Import**: Import existing maps from Freeplane, XMind, and Mind Manager. Export mind maps to PDF, SVG, Freeplane, and other formats
- **🔗 Document Linking**: Integrate external documents and resources into your mind maps
- **📤 Embed & Share**: Easily embed mind maps into web pages, blogs, and documentation
- **🆓 100% Free**: Access all features without any restrictions
- **🔍 Search & Navigation**: Quickly find content across all your mind maps
- **📝 Rich Content**: Add detailed notes, links, and formatted text to nodes
- **🔒 Self-hosted**: Complete control over your data with on-premise deployment
- **🌐 Multi-language**: Available in multiple languages (English, Spanish, French, German, Italian, Russian, Chinese, and more)
- **🔌 REST API**: Full REST API for integration and automation
- **📈 User Management**: Authentication with database, Google OAuth, Facebook OAuth, and LDAP support
- **💾 Supported Persistence**: PostgreSQL v15+ (recommended for production), MySQL v8+ (supported for production), HSQLDB v2.7+ (development/testing only)
- **🐳 Docker Deployment**: Production-ready Docker images available on [Docker Hub](https://hub.docker.com/r/wisemapping/wisemapping)



## Deployment (Production - Recommended)

For production deployments, follow the official Docker images and instructions on Docker Hub: `https://hub.docker.com/r/wisemapping/wisemapping`.

## Development (Local)

The following steps are intended for local development only (not production). For production, see the Deployment section above.

## Prerequisites

    * JDK 24 or higher
    * Maven v3.x or higher ([http://maven.apache.org/])
    * Yarn v12 or higher
    * Node v24 or higher

## Option 1: Quick Start with Docker Compose

The following command line will start WiseMapping locally using HSQLDB in memory for development purposes:



```
$ mvn -f wise-api/pom.xml package
$ docker compose up --build
```

Application will start at http://localhost/c/login. You can login using *test@wisemapping.org* and password *test*

## Option 2: Start Frontend and Backend API

### Compile and Start API

```
$ mvn -f wise-api/pom.xml package
$ cd wise-api
$ mvn spring-boot:run
```

### Compile and Start Frontend

You need to checkout https://github.com/wisemapping/wisemapping-frontend first. Then, follow the next steps:

```
$ export NODE_OPTIONS=--openssl-legacy-provider
$ export APP_CONFIG_TYPE="file:dev"

$ cd wisemapping-frontend
$ yarn install 
$ yarn build

$ cd packages/webapp; yarn start
```
Application will start at http://localhost:3000/c/login. You can login using *test@wisemapping.org* and password *test*

# Supportability Matrix

## Databases

* **PostgreSQL v15 or higher** (Recommended for production)
* **MySQL v8 or higher** (Supported for production)
* **Hsqldb v2.7 or higher** (Development and testing only - NOT for production)

# Configuration

WiseMapping backend is based on SpringBoot v3 and it's highly customizable. Additional documentation can be found [here](https://docs.spring.io/spring-boot/3.3/reference/features/external-config.html)

The perfered option is to extended by overwriting [application.yaml](https://github.com/wisemapping/wisemapping-open-source/blob/develop/wise-api/src/main/resources/application.yml)

```
$ java -jar target/wisemapping-api.jar --spring.config.additional-location=../../wise-conf/app.yml
```

For example, this [example](https://github.com/wisemapping/wisemapping-open-source/blob/develop/config/database/postgresql/app-postgresql.yaml) configure PostgreSQL as database.

# Members

## Founders

   * Paulo Veiga <pveiga@wisemapping.com>
   * Pablo Luna <pablo@wisemapping.com>

## Past Individual Contributors

   * Ezequiel Bergamaschi <ezequielbergamaschi@gmail.com>
   
## License

The source code is Licensed under the WiseMapping Open License, Version 1.0 (the “License”);
You may obtain a copy of the License at: [https://github.com/wisemapping/wisemapping-open-source/blob/develop/LICENSE.md](https://github.com/wisemapping/wisemapping-open-source/blob/develop/LICENSE.md)


## 📚 Documentation

- **[API Documentation](doc/api-documentation/README.md)** - Complete REST API documentation with examples
- **[Backend Documentation](doc/api-documentation/backend/README.md)** - Backend-specific documentation including telemetry and OpenAPI specs
- **[Deployment Guide](distribution/)** - Docker and deployment documentation

> This README focuses on development setup. For production, use the Deployment section below.