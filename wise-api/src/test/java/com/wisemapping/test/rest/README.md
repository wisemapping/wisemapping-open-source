# WiseMapping API REST Tests

This directory contains integration tests for the WiseMapping REST API. They are
full `@SpringBootTest` tests: each one boots the application on a random port and
drives it over real HTTP with `TestRestTemplate` (or `MockMvc`, in
`RestAppControllerTest`).

## Test Users

The tests use pre-configured test users that are initialized in `data-hsqldb.sql`:

### Admin User
- **Email**: `admin@wisemapping.org`
- **Password**: `testAdmin123`
- **Role**: Administrator

### Regular Test User
- **Email**: `test@wisemapping.org`
- **Password**: `password`
- **Role**: Regular user

Administrator rights are not stored on the account row. They come from
`app.admin.user` in `src/test/resources/application-test.yml`, which is set to
`admin@wisemapping.org`; any account with that email is treated as an admin.

## Test Configuration

Everything these tests run against is defined in
`src/test/resources/application-test.yml`, activated by `@ActiveProfiles("test")`.
The parts worth knowing:

- **Database**: in-memory HSQLDB at `jdbc:hsqldb:mem:test-${random.uuid}`. The
  `${random.uuid}` is resolved when a Spring context is built, so **each context
  gets its own database**.
- **Schema and seed data**: created only from `schema-hsqldb.sql` and
  `data-hsqldb.sql` (both in `src/main/resources/`). JPA DDL generation is off
  (`ddl-auto: none`), so an entity change that is not mirrored in the SQL files
  will fail here.
- **Caching off**: Hibernate L2 cache, query cache and Spring's cache abstraction
  are all disabled, so tests see writes immediately.
- **Scheduled jobs off**: every `app.batch.*` job (spam detection, history purge,
  inactive-user/map cleanup) is disabled so nothing mutates data mid-test.
- **HTTP Basic on**: `app.api.http-basic-enabled: true`, which is what makes
  `restTemplate.withBasicAuth(...)` work.
- **Locale pinned to `en`** so message assertions are stable.

### Password Requirements
All user passwords must meet the following requirements:
- Minimum length: **8 characters** (defined by `Account.MIN_PASSWORD_LENGTH_SIZE`)
- Maximum length: **40 characters** (defined by `Account.MAX_PASSWORD_LENGTH_SIZE`)

### Test Data Setup
- Test users are initialized via SQL in `src/main/resources/data-hsqldb.sql`
- Passwords are stored as SHA-1 hashes with the `ENC:` prefix
- Most classes create their own throwaway users through the API via
  `RestHelper.createUserViaApi(...)` / `createTestUser(...)`
- `TestDataManager` offers unique-email/title generators and a `cleanupTestData()`
  helper. Only `RestUserControllerTest` uses it today — it is not a suite-wide
  cleanup mechanism

### Common Test Patterns

#### Creating Test Users
```java
// Using admin credentials
final TestRestTemplate adminTemplate = restTemplate.withBasicAuth("admin@wisemapping.org", "testAdmin123");

// Create a new user
final RestUser newUser = new RestUser();
newUser.setEmail("test-" + System.nanoTime() + "@example.org");
newUser.setPassword("testPassword123"); // Must be 8+ characters
```

#### Authentication
```java
// Authenticate as regular user
final TestRestTemplate userTemplate = restTemplate.withBasicAuth("test@wisemapping.org", "password");

// Authenticate as admin
final TestRestTemplate adminTemplate = restTemplate.withBasicAuth("admin@wisemapping.org", "testAdmin123");
```

#### Request headers
`RestHelper.createHeaders(mediaType)` sets **both** `Content-Type` and `Accept` to
the given type. That is wrong for the endpoints that take a `text/plain` body but
answer `application/json` (`PUT /{id}/starred`, `PUT /{id}/description`,
`PUT /{id}/title`): `Accept: text/plain` earns a silent `406` from them. For those,
set the content type only:

```java
final HttpHeaders textBody = new HttpHeaders();
textBody.setContentType(MediaType.TEXT_PLAIN);
```

Also note `TestRestTemplate.put(...)` returns `void` and does not throw on an error
status, so a rejected write looks like a successful one. Prefer `exchange(...)` and
assert the status.

## Test Classes

- **AdminControllerTest**: admin endpoints — user and map management, pagination,
  search, spam status, suspension, map XML
- **AdminSystemControllerTest**: system admin endpoints (`/system/info`, `/system/health`)
- **RestAccountControllerTest**: account management
- **RestAppControllerTest**: the runtime config blob at `/api/restful/app/config` (MockMvc)
- **RestJwtAuthControllerTest**: JWT login
- **RestLabelControllerTest**: label operations
- **RestMindmapControllerTest**: mindmap CRUD, history, collaboration, metadata
- **RestMindmapDeleteWithLabelsTest**: map deletion with labels attached
- **RestMindmapDeletionTest**: map deletion and cascade behaviour
- **RestUserControllerTest**: user registration and authentication
- **RestHelper**: shared static helpers (headers, user creation, map creation)
- **TestDataManager**: generators for unique test data, plus an opt-in cleanup helper

## Running Tests

This repo has no parent POM, so Maven must be pointed at `wise-api/pom.xml`
(or run from inside `wise-api/`).

```bash
# Run all tests
mvn -f wise-api/pom.xml test

# Skip coverage instrumentation (faster)
mvn -f wise-api/pom.xml test -Djacoco.skip=true

# Run a specific test class
mvn -f wise-api/pom.xml test -Dtest=RestUserControllerTest

# Run a single test method
mvn -f wise-api/pom.xml test -Dtest=RestUserControllerTest#shouldRegisterNewUserSuccessfully
```

The project targets **Java 26** (the `java.version` property in
`wise-api/pom.xml`). No extra JVM flags need to be passed by hand — see note 2.

## Important Notes

1. **Password Changes**: If you change test passwords, you must also update:
   - The constant in the test class (e.g. `ADMIN_PASSWORD`, `REGULAR_PASSWORD`)
   - The SHA-1 hash in `data-hsqldb.sql`
   - The password in `TestDataManager.java` if applicable

2. **Mockito**: nothing to configure manually. The surefire `argLine` in
   `wise-api/pom.xml` already passes `-XX:+EnableDynamicAgentLoading`,
   `-Djdk.attach.allowAttachSelf=true` and `-Dnet.bytebuddy.experimental=true`, and
   `src/test/resources/mockito-extensions/org.mockito.plugins.MockMaker` pins the
   subclass-based `ByteBuddyMockMaker`.

3. **Test Isolation**: every REST test class in this directory is annotated

   ```java
   @SpringBootTest(classes = {AppConfig.class}, webEnvironment = RANDOM_PORT)
   @ActiveProfiles("test")
   @DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
   ```

   which gives:
   - `RANDOM_PORT` — no port collisions between classes or with a local dev server
   - `@ActiveProfiles("test")` — the configuration described above
   - `@DirtiesContext(AFTER_CLASS)` — the context is closed after each class, so the
     next class builds a fresh one and, because the datasource URL embeds
     `${random.uuid}`, a **fresh empty database**. This is what keeps classes from
     polluting each other: all of them declare an identical `@SpringBootTest`
     configuration, so without `@DirtiesContext` Spring would hand every one of
     them the same cached context and the same HSQLDB instance, and the maps,
     labels and users one class creates would be visible to the next.
   - Within a class, isolation is the test author's job. Most classes create a new
     user in `@BeforeEach` and work inside it; the ones that touch the seeded
     `test@wisemapping.org` / map id 1 rows share them across methods.

4. **Known performance cost — ten Spring context reloads**: the consequence of
   note 3 is that a full `mvn test` builds and tears down one Spring context per
   REST test class (ten of them; nineteen contexts across the whole suite,
   counting the DAO/service/scheduler integration tests). Each one re-runs
   `schema-hsqldb.sql` and `data-hsqldb.sql` and restarts Tomcat.

   This is a deliberate trade, not an oversight: correctness over speed. Dropping
   `@DirtiesContext` would collapse the ten contexts into one and is the obvious
   "free" speed-up, but it is **not** free — it removes the only thing currently
   preventing cross-class data pollution (see note 3). Anyone who wants that win
   needs to do the isolation work first: give each class a distinct context key,
   or make every class independent of global state (seeded ids, `/admin` listings,
   map counts) and clean up after itself. Until then, leave the annotations alone.

5. **Log Level**: test logging is configured in `src/test/resources/logback-test.xml`
   — root at `WARN`, with `com.wisemapping` at `INFO` — to keep output readable.
