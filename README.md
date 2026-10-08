# WiseMapping Open Source

WiseMapping is a free, open-source, web-based mind mapping tool designed for individuals, teams, and educational institutions. It enables users to create, share, and collaborate on mind maps with role-based access control, facilitating brainstorming sessions, project planning, and knowledge management. Built with modern open standards technologies like SVG and React, WiseMapping provides a versatile and user-friendly platform to visualize and organize complex information effectively. The open-source codebase powers https://www.wisemapping.com, ensuring reliability and continuity in its development.

## 🎯 Capabilities

WiseMapping provides a comprehensive set of features for creating, managing and sharing mind maps.

### ✏️ Mind map editing

- **Topics & structure**: Unlimited hierarchy, drag-and-drop reordering, indent/outdent, sibling and child creation, multi-selection, copy/paste (including paste-as-child), and detached free-floating topics (`Ctrl`/`⌘` + drag to disconnect)
- **Layouts**: Classic radial mind map (horizontal, balanced) or top-down tree (vertical)
- **Topic styling**: Shapes (rectangle, rounded rectangle, ellipse, line, none, image), fill and border colours, border styles (solid, dashed, dotted), and font family, size, weight, style and colour
- **Connector styles**: Seven connector shapes — thin and thick curved, arc, straight and curved polyline, heartbeat and neuron — with per-branch colour overrides
- **Relationships**: Free-form connections between any two topics, with configurable colour, line type, start/end arrowheads and solid/dashed/dotted strokes
- **Themes**: Seven built-in themes — Classic, Prism, Robot, Sunrise, Ocean, Aurora and Retro — plus light and dark mode
- **Canvas**: Configurable background colour and pattern (solid, grid, dots) with adjustable grid size and colour; pan, zoom, zoom-to-fit, two-finger/trackpad zoom, and remembered scroll and zoom position
- **Undo/redo**: Command-based undo history covering every editing action

### 📝 Content on topics

- **Rich-text notes**: Bold, italic, underline, strikethrough, bulleted and numbered lists, inline links and icons
- **Document linking**: Attach external URLs and resources to any topic
- **Icons**: Searchable SVG icon gallery with a "frequently used" section, plus the full emoji set
- **Topic images**: Render a topic as an image picked from the gallery

### 🔍 Navigating & reviewing

- **Find in map**: Incremental search with match-by-match navigation across every topic
- **Outline view**: Hierarchical outline of the whole map, with note and link indicators
- **Expand & collapse**: Per-branch collapse, expand/collapse all, and expand to a chosen depth
- **Keyboard-first editing**: Comprehensive shortcuts with an in-editor reference pane for both Windows/Linux and macOS bindings
- **Version history**: Browse earlier revisions of a map and revert to any of them
- **Print**: Print-optimised rendering of the current map

### 📊 Import & export

- **Import**: WiseMapping XML (`.wxml`), Freemind (`.mm`), Freeplane (`.mmx`), XMind (`.xmind`), MindManager (`.mmap`) and OPML
- **Export as image**: SVG, PNG, JPG and PDF
- **Export as document**: Plain text and Markdown
- **Export to other tools**: WiseMapping XML, Freemind and Freeplane

### 👥 Sharing & collaboration

- **Role-based sharing**: Invite collaborators per map as owner, editor or viewer, enforced by map-level permission checks on every request
- **Edit locking**: A map being edited is locked so concurrent sessions cannot overwrite each other's work
- **Publish & embed**: Make a map publicly readable and embed it into web pages, blogs and documentation
- **Organisation**: Colour-coded labels, starred maps, filtered views (all, owned, shared, starred, public, by label), duplicate, rename, bulk delete, and per-map info

### 🔐 Accounts & administration

- **Authentication**: Database accounts, Google and Facebook OAuth2, and LDAP, with JWT-based sessions
- **Account self-service**: Registration with optional reCAPTCHA, email activation, link-based password reset, and account deletion
- **Admin console**: Account management and system status pages for administrators
- **Spam & abuse controls**: Spam detection on write and as a scheduled batch, with automatic user suspension
- **Housekeeping**: Scheduled history purge plus inactive-user and inactive-map cleanup
- **Telemetry**: Micrometer metrics with Prometheus and OpenTelemetry/OTLP exporters, disabled by default and opt-in per operator

### 🌐 Platform

- **Multi-platform**: Runs in any modern browser on desktop or mobile, built on open standards (SVG, React)
- **Multi-language**: 13 UI languages — English, Spanish, French, German, Italian, Portuguese, Russian, Ukrainian, Chinese (Simplified and Traditional), Japanese, Hindi and Arabic (right-to-left)
- **REST API**: Full REST API for integration and automation, documented with OpenAPI
- **Self-hosted**: Complete control over your data with on-premise deployment
- **Supported persistence**: PostgreSQL v15+ (recommended for production), MySQL v8+ and MariaDB v11.4+ (supported for production), HSQLDB v2.7+ (development/testing only)
- **Docker deployment**: Production-ready multi-architecture (amd64 and arm64) images on [Docker Hub](https://hub.docker.com/r/wisemapping/wisemapping)
- **🆓 100% free**: Every feature, without restrictions

## Deployment (Production - Recommended)

For production deployments, follow the official Docker images and instructions on Docker Hub: `https://hub.docker.com/r/wisemapping/wisemapping`.

## Development (Local)

The following steps are intended for local development only (not production). For production, see the Deployment section above.

## Prerequisites

* JDK 26 or higher
* Maven v3.x or higher (<http://maven.apache.org/>)
* Yarn v4 or higher
* Node v24 or higher

## Option 1: Quick Start with Docker Compose

The fastest way to get a full stack running is one of the reference Compose overlays under
`distribution/`. They run the published `wisemapping/wisemapping` image against a real database,
so no local build is required:

```
$ cd distribution/app-postgresql     # or app-mysql / app-mariadb
$ cp .env.example .env               # set WISEMAPPING_DATA_DIR and POSTGRES_PASSWORD in it
$ mkdir -p <WISEMAPPING_DATA_DIR>/logs
$ docker compose up -d
```

`WISEMAPPING_DATA_DIR` and the database password are mandatory — Compose fails fast if either is
unset. See `distribution/app-postgresql/README.md` for the full set of variables.

Application will start at http://localhost/c/login. You can login using *test@wisemapping.org*
with password *password*, or as an administrator with *admin@wisemapping.org* / *testAdmin123*.

## Option 2: Start Frontend and Backend API

### Compile and Start API

```
$ mvn -f wise-api/pom.xml package
$ cd wise-api
$ mvn spring-boot:run
```

The API starts on :8080 backed by an in-memory HSQLDB, so no database setup is needed for
development. Point it at PostgreSQL, MySQL or MariaDB with an external config overlay (see
[Configuration](#configuration)).

### Compile and Start Frontend

You need to checkout https://github.com/wisemapping/wisemapping-frontend first. Then, follow the next steps:

```
$ export APP_CONFIG_TYPE="file:dev"

$ cd wisemapping-frontend
$ yarn install
$ yarn build

$ cd packages/webapp; yarn start
```
Application will start at http://localhost:3000/c/login. The dev server proxies `/api` to the API on
:8080. You can login using *test@wisemapping.org* with password *password*, or as an administrator
with *admin@wisemapping.org* / *testAdmin123*.

## Supportability Matrix

### Databases

* **PostgreSQL v15 or higher** (Recommended for production)
* **MySQL v8 or higher** (Supported for production)
* **MariaDB v11.4 or higher** (Supported for production)
* **Hsqldb v2.7 or higher** (Development and testing only - NOT for production)

## Configuration

WiseMapping backend is based on Spring Boot v4 and it's highly customizable. Additional documentation can be found [here](https://docs.spring.io/spring-boot/reference/features/external-config.html)

The preferred option is to extend it by overriding [application.yml](https://github.com/wisemapping/wisemapping-open-source/blob/develop/wise-api/src/main/resources/application.yml)

```
$ java -jar wise-api/target/wisemapping-api.jar --spring.config.additional-location=file:./app.yml
```

For example, this [overlay](https://github.com/wisemapping/wisemapping-open-source/blob/develop/distribution/app-postgresql/app.yml)
configures PostgreSQL as the database. Equivalent overlays for MySQL and MariaDB live alongside it
under [`distribution/`](https://github.com/wisemapping/wisemapping-open-source/tree/develop/distribution).

## Members

### Founders

   * Paulo Veiga <pveiga@wisemapping.com>
   * Pablo Luna <pablo@wisemapping.com>

### Past Individual Contributors

   * Ezequiel Bergamaschi <ezequielbergamaschi@gmail.com>
   
## License

The source code is Licensed under the WiseMapping Open License, Version 1.0 (the “License”);
You may obtain a copy of the License at: [https://github.com/wisemapping/wisemapping-open-source/blob/develop/LICENSE.md](https://github.com/wisemapping/wisemapping-open-source/blob/develop/LICENSE.md)


## 📚 Documentation

- **[API Documentation](doc/api-documentation/README.md)** - Complete REST API documentation with examples
- **[Backend Documentation](doc/api-documentation/backend/README.md)** - Backend-specific documentation including telemetry and OpenAPI specs
- **[Deployment Guide](distribution/)** - Docker and deployment documentation

> This README focuses on development setup. For production, use the Deployment section above.