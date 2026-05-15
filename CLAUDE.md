# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this repo is

The Spring Boot backend for WiseMapping — a REST API + persistence + auth layer that the separately-versioned frontend (`wisemapping-frontend`) consumes. Single-module Maven project under `wise-api/`. Java 24, Spring Boot 4.0.x, JPA/Hibernate.

The frontend is **not** in this repo. Local dev requires checking out `https://github.com/wisemapping/wisemapping-frontend` separately and pointing it at this API (see README "Option 2").

## Common commands

All commands below run against `wise-api/pom.xml`. The repo has no parent POM — invoke Maven with `-f wise-api/pom.xml` from the root, or `cd wise-api` first.

```sh
mvn -f wise-api/pom.xml package                  # full build (fat jar at wise-api/target/wisemapping-api.jar)
mvn -f wise-api/pom.xml package -DskipTests      # skip tests
cd wise-api && mvn spring-boot:run               # run locally on :8080 (HSQLDB in-memory by default)

# Tests (JUnit 5 via surefire)
cd wise-api && mvn test
cd wise-api && mvn test -Dtest=MindmapControllerTest                  # single class
cd wise-api && mvn test -Dtest=MindmapControllerTest#shouldCreateMap  # single method

# Run with an external config override (preferred way to point at PostgreSQL/MySQL/SMTP/etc.)
java -jar wise-api/target/wisemapping-api.jar \
  --spring.config.additional-location=path/to/app.yml

# Full stack via Docker (HSQLDB in-memory, builds fat jar first)
mvn -f wise-api/pom.xml package
docker compose up --build       # uses distribution/app-postgresql/docker-compose.yml if invoked from there

# PostgreSQL stack
cd distribution/app-postgresql && docker compose up --build
```

Default seed credentials in dev: `test@wisemapping.org` / `test`.

## Configuration model

`wise-api/src/main/resources/application.yml` holds defaults for every feature. Don't edit it for environment-specific values — override via Spring's external config (`--spring.config.additional-location=...`) or environment variables. Example overlays live in `config/database/` (e.g. `app-postgresql.yaml`).

Key namespaces (read `application.yml` before changing behavior):
- `spring.datasource` — defaults to embedded HSQLDB. Switch to `postgresql`/`mysql`/`mariadb` by overriding driver/url/username/password; the matching `schema-*.sql` and `data-*.sql` under `src/main/resources/` are auto-applied. Reference overlays live under `distribution/app-{postgresql,mysql,mariadb}/app.yml`.
- `spring.security.oauth2.client` — Google/Facebook OAuth2; client IDs/secrets are expected to be supplied via overlay.
- `spring.ldap` + `app.security.ldap` — LDAP auth (see `doc/ldap/README.md`). Disabled by default.
- `app.jwt` — JWT signing key + expiration (used by `JwtTokenUtil`, `JwtAuthController`).
- `app.site.*` — base URLs the API embeds in emails and OAuth redirects.
- `management.*` — Actuator, Prometheus, OpenTelemetry. Spam detection, history cleanup, and inactive-user/inactive-map cleanup are scheduled jobs configured here too.

## Architecture (big picture)

Layered Spring Boot app. Entry point: `com.wisemapping.Application`.

- **`rest/`** — `@RestController` HTTP surface. One controller per top-level resource: `AccountController`, `MindmapController`, `LabelController`, `AdminController`, `UserController`, `JwtAuthController`, `OAuth2Controller`, `AppController` (the last serves the runtime config blob the frontend consumes at `/api/restful/app/config` — including `analyticsAccount`, OAuth URLs, recaptcha, registration flag). DTOs live in `rest/model/`.
- **`service/`** — Business logic. `MindmapService` (CRUD + history + collaboration), `UserService`, `LabelService`, `MailerService`, `RecaptchaService`, `NotificationService`. The spam pipeline is a separate cluster: `SpamDetectionService`, `SpamDetectionBatchService`, `SpamUserSuspensionService`, plus strategies under `service/spam/`. History retention is implemented as a chain-of-responsibility: `HistoryCleanupHandler` → `Phase1HistoryCleanupHandler` → `Phase2HistoryCleanupHandler`, driven by `HistoryPurgeService`.
- **`security/`** — Spring Security wiring. `AuthenticationProvider` (DB) and `AuthenticationProviderLDAP` plug in based on config. JWT is the session mechanism (`JwtTokenUtil`); OAuth2 success funnels through `OAuth2AuthenticationSuccessHandler`. **Map-level authorization** is AOP-based: `MapPermissionsSecurityAdvice` + `ReadSecurityAdvise` / `UpdateSecurityAdvise` intercept service methods and enforce `MapAccessPermission` against `Collaboration`/`CollaborationRole` records. Don't bypass these by calling DAOs directly from controllers.
- **`model/`** — JPA entities (`Mindmap`, `Account`, `Collaboration`, `MindmapLabel`, `MindMapHistory`, etc.) plus enums like `CollaborationRole`, `SpamStrategyType`, `SuspensionReason`.
- **`dao/`** — Repositories.
- **`mindmap/`** — Mind-map XML domain (parsing/serializing the on-disk format, validated against `mindmap.xsd`).
- **`scheduler/`** — `@Scheduled` jobs (history purge, inactive-user/inactive-map cleanup, spam batch).
- **`filter/`** — Servlet filters; `MindmapFilter` in `rest/` handles per-request map context for permission advice.
- **`listener/`**, **`metrics/`** — JPA event listeners and Micrometer metrics; OTLP + Prometheus exporters are wired in `pom.xml`.
- **`config/`** — `AppConfig` (top-level beans), `GlobalExceptionHandler` (consistent JSON error responses), `LdapProperties`.

Cross-cutting things to know:
- **Caching**: Hibernate L2 cache via EHCache (`ehcache.xml`). Adding a `@Cacheable` entity/region requires an entry there.
- **i18n**: User-facing strings live in `messages_*.properties` (resolved by Spring `MessageSource`). Email templates use Velocity (`mail/`).
- **Spam**: Detection runs both on write (`SpamDetectionService` invoked from `MindmapServiceImpl`) and as a scheduled batch. Keyword/domain lists ship in `resources/spam/` and `disposable-email-domains.txt`.
- **Resilience4j**: Used to circuit-break external calls (recaptcha, mail). Configured under `resilience4j.*` in `application.yml`.

## Distribution / deployment

- `distribution/api/Dockerfile` — image for the API jar.
- `distribution/app/Dockerfile` + `nginx.conf` + `supervisord.conf` — combined image (nginx serving the prebuilt frontend + the API behind it). This is what production uses.
- `distribution/app-postgresql/docker-compose.yml` — reference compose for PostgreSQL deployment, with a sample overlay at `app.yml`.

The README directs production users to the published images on Docker Hub (`wisemapping/wisemapping`) rather than building locally.

## Code conventions

Patterns observed across the existing code — match them when adding new files. These are not enforced by tooling, so they're easy to break by accident.

**License header** — every `.java` file starts with the WiseMapping Public License v1.0 block (`Copyright [2007-2025] [wisemapping]`, links to `LICENSE.md`). Copy verbatim from any existing file (e.g. `rest/MindmapController.java`). New files without it will be inconsistent and may fail review.

**Package layout & naming**
- Controllers in `rest/`, suffix `Controller`. DTOs returned/accepted by controllers live in `rest/model/` and are distinct from JPA entities in `model/` — never return entities from a controller.
- Services follow an interface + `*Impl` split (`MindmapService` / `MindmapServiceImpl`). The interface is the injection target.
- Exceptions extend `ClientException` (→ `WiseMappingException`); each carries a `MSG_KEY` constant that resolves through the message bundle (see i18n below).
- Nullability is annotated with `org.jetbrains.annotations.@NotNull` / `@Nullable` (not `jakarta.annotation`, not `javax`). Apply on parameters and return types.

**Dependency injection** — `@Autowired` field injection is the existing house style (see `MindmapServiceImpl`, `GlobalExceptionHandler`). Stay consistent within a file. Service classes are `@Service("beanName")` with an explicit qualifier name.

**Transactions** — `@Transactional` lives at the service layer (typically class-level on `*Impl`, with `Propagation.REQUIRED`). Do not put `@Transactional` on controllers or DAOs.

**Authorization** — Map-scoped operations are guarded by `@PreAuthorize` on service methods, evaluated by `MapPermissionsSecurityAdvice` against `Collaboration` records. New methods that touch a `Mindmap` must declare a `@PreAuthorize` rule; never enforce permissions inline in the controller.

**Logging** — SLF4J. Declare as `private static final Logger logger = LoggerFactory.getLogger(Foo.class);` (the field name is `logger`, not `log` or `LOG`). Use parameterized messages (`logger.warn("msg {}", id)`), never string concatenation. No `System.out` / `printStackTrace`.

**i18n (user-facing strings)**
- All messages shown to end users go through `src/main/resources/messages*.properties`. The base bundle is `messages.properties` (English defaults); per-locale files are `messages_<lang>.properties` for `ar de en es fr hi it ja pt ru uk zh zh-CN`. **Adding a new key requires updating every locale file** (English first, others can copy English until translated).
- The integration point is `ClientException` subclasses: each defines `private static final String MSG_KEY = "..."` matching a properties key, and `GlobalExceptionHandler` resolves it via `MessageSource` using the request `Locale`. To add a user-visible error, write a new `ClientException` subclass with a `MSG_KEY`, add the key to every bundle, and let the global handler render it.
- Email templates are Velocity files under `resources/mail/` and use the same bundle for substitutions. Don't hardcode strings in templates.
- Server logs and exception messages passed to other services are **not** localized — keep those English.

**Validation** — input validation belongs in `validator/` (`@Component`-based validators like `MapInfoValidator`, `HtmlContentValidator`). Throw `ValidationException` / `HtmlContentValidationException` with a `MSG_KEY` rather than returning error strings.

**SQL & schema** — schema lives in `schema-{hsqldb,mysql,mariadb,postgresql}.sql` and seed data in `data-*.sql`. A schema change must be applied to **all four** dialect files; CI runs against HSQLDB but production is PostgreSQL. `schema-mariadb.sql` / `data-mariadb.sql` should normally be kept in lockstep with the MySQL variants.

### Database Standards & Type Mappings
To ensure cross-platform consistency and resolve encoding/case-sensitivity issues (ref: Issue #69), follow these standards:

1.  **Naming Convention**: All table and column names must be **lowercase**. (Linux environments are case-sensitive; lowercase ensures compatibility with Hibernate's default naming strategy).
2.  **MySQL Encoding**: Always use `CHARACTER SET UTF8MB4` for MySQL tables and string columns to support emojis and international characters.
3.  **Data Type Mapping Table**:

| Logical Type | MySQL | PostgreSQL | HSQLDB | Typical Usage |
| :--- | :--- | :--- | :--- | :--- |
| **Primary Key (Auto)** | `INTEGER ... AUTO_INCREMENT` | `SERIAL` | `INTEGER ... IDENTITY` | Database IDs (Collaborator, Mindmap, etc.) |
| **Dates & Times** | `DATETIME` | `TIMESTAMP` | `DATETIME` | Creation, modification, and activation dates |
| **Booleans** | `BOOL` (TinyInt) | `BOOL` | `BOOLEAN` | Flags (isPublic, suspended, allowSendEmail) |
| **Large Text** | `MEDIUMTEXT` | `TEXT` | `LONGVARCHAR` | JSON settings, OAuth tokens, long descriptions |
| **Large Binary** | `MEDIUMBLOB` | `BYTEA` | `LONGVARBINARY` | Compressed Mindmap XML and history snapshots |
| **Standard String** | `VARCHAR(255)` | `VARCHAR(255)` | `VARCHAR(255)` | Titles, emails, names, and identifiers |

*Note: `MEDIUMTEXT`/`MEDIUMBLOB` (16MB) is preferred over `TEXT`/`BLOB` (64KB) for MySQL to prevent truncation of large mindmaps or user settings.*

**Spam allowlist** — trusted source domains live in `resources/spam/popular-domain-whitelist.yml`. Bare entries match the exact host; use `"*.example.com"` for subdomain wildcards (existing pattern). Group additions under a comment header by region/category.

**Tests** — JUnit 5 (`junit-vintage-engine` is excluded; don't import JUnit 4). Spring Boot test slices preferred over full-context tests when possible. Test class naming mirrors the SUT: `FooService` → `FooServiceTest`.

## Documentation pointers

- `doc/api-documentation/` — REST API reference + backend specifics (telemetry, OpenAPI).
- `doc/ldap/README.md` — LDAP setup. The `app.security.ldap` block in `application.yml` is heavily commented and is the source of truth.