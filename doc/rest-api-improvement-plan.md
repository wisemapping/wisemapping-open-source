# REST API Improvement Plan

Status: **P5a (coverage tooling) landed. Everything else proposed.**

Scope: the `com.wisemapping.rest` HTTP surface, its error contract, and its test
coverage. Each item below is self-contained and can be landed on its own. Work
them **one at a time**, running `cd wise-api && mvn test` after each.

---

## Baseline (measured 2026-10-05, commit `e5d57f18`)

| Fact | Value |
| :--- | :--- |
| Full suite | **577 tests, 0 failures, 0 errors, 1 skipped** — green, before and after adding JaCoCo |
| REST test classes | 10 (`wise-api/src/test/java/com/wisemapping/test/rest/`), ~5.6k LOC |
| Controller LOC | 3,012 across 8 controllers (`AdminController` 1,005, `MindmapController` 1,042) |
| Coverage tooling | JaCoCo 0.8.13, wired in P5a ✅ |
| **Project coverage** | **54.1% instruction, 45.2% branch, 54.2% line, 55.7% method** |
| **`com.wisemapping.rest`** | **66.9% instruction, 52.2% branch, 74.8% method** |
| Endpoint count | ~55 mappings across 8 controllers |

The suite is healthy and endpoint breadth is decent. The gaps are concentrated in
the **error contract** (verified broken, see P1) and in a handful of endpoints
that have no test at all (P3).

### Coverage by package (`mvn test`, excluding entities/DTOs)

| Package | instr% | branch% | instr |
| :--- | ---: | ---: | ---: |
| `com.wisemapping.service` | 54.6% | 46.1% | 7,509 |
| `com.wisemapping.rest` | 66.9% | 52.2% | 5,093 |
| `com.wisemapping.dao` | 46.7% | 39.9% | 4,436 |
| `com.wisemapping.service.spam` | 69.8% | 58.7% | 3,412 |
| `com.wisemapping.security` | **35.0%** | **27.7%** | 2,765 |
| `com.wisemapping.config` | 45.6% | **13.8%** | 1,641 |
| `com.wisemapping.mindmap.model` | **25.6%** | **14.6%** | 935 |
| `com.wisemapping.mindmap.parser` | 90.7% | 75.8% | 723 |
| `com.wisemapping.mindmap.utils` | 31.0% | 33.3% | 675 |
| `com.wisemapping.validator` | 74.0% | 68.4% | 577 |
| `com.wisemapping.exceptions` | 35.1% | 0.0% | 576 |
| `com.wisemapping.view` | **18.8%** | 36.4% | 357 |

### Coverage per REST class

| Class | instr% | missed instr | branch% |
| :--- | ---: | ---: | ---: |
| `AdminController` | 60.8% | **899** | 47.5% |
| `MindmapController` | 75.3% | **446** | 65.1% |
| `JwtAuthController` | **42.0%** | 76 | **14.3%** |
| `UserController` | 76.7% | 50 | 40.9% |
| `AccountController` | 76.5% | 48 | 53.8% |
| `MindmapFilter` | 66.3% | 32 | **12.5%** |
| `AppController` | 73.5% | 30 | 50.0% |
| `OAuth2Controller` | **19.4%** | 29 | — |
| `LabelController` | 80.6% | 24 | 50.0% |

### The headline gap: the error contract is almost entirely unexercised

`GlobalExceptionHandler` defines **26 `@ExceptionHandler` methods. Tests exercise 9.**
The 17 that never run include every authentication-failure path
(`handleBadCredentialsException`, `handleAccountDisabledException`,
`handleAccountSuspendedException`, `handleAccountSuspendedInactivityException`,
`handleLockedException`, `handleWrongAuthenticationTypeException`), the spam and
rate-limit paths (`handleSpamContentException`, `handleRateLimitExceeded`), the
lock path (`handleLockException`), `handleWiseMappingException`,
`handleUserRegistrationException`, `handleOAuthErrors`, and the catch-all
`handleServerErrors`.

That last one is the whole story behind **P1**: the only reason a blanket
`Exception` → 500 handler could swallow every Spring MVC binding error unnoticed
is that **no test ever asserted a status code on a malformed request.** The
coverage number names the blind spot precisely.

Branch coverage of `com.wisemapping.config` is **13.8%** and of
`com.wisemapping.exceptions` is **0.0%** for the same reason.

### Endpoints and methods with zero coverage

Confirmed by JaCoCo, which matches the static read in P3 and adds some:

| Class | Method (line) | Note |
| :--- | :--- | :--- |
| `JwtAuthController` | `logout` (75) | 43 instr, 0/8 branches — whole endpoint |
| `OAuth2Controller` | `confirmAccountSync` (59) | whole controller |
| `MindmapController` | `validateNoteContent` (745) | whole endpoint |
| `MindmapController` | `retrieveDocument` (350) | the `/{id}/{hid}/document/xml` overload |
| `MindmapController` | `buildValidationException` (977) | no test reaches map-title validation failure |
| `AdminController` | `activateUser` (502) | admin activation endpoint |
| `AdminController` | `getUserMaps` (585) | `/admin/users/{id}/maps` |
| `AdminController` | `deleteMap` (710) | `/admin/maps/{id}` DELETE |
| `AdminController` | `getBuildInfo` (894) | 176 instr — the single largest uncovered method |
| `AdminController` | `getUserByFacebookId`, `removeFacebookAccount`, `isFacebookEnabled` | Facebook-unlink surface, entirely untested |
| `AdminController` | 7 lambdas at 811–878 | `getSystemInfo` sub-sections (284 instr) never evaluated |
| `MindmapFilter` | `MY_MAPS`, `PUBLIC`, `STARRED`, `SHARED_WITH_ME`, `LabelFilter.accept` | **4 of 5 filters + the label fallback never run** — only `ALL` is covered, hence 12.5% branch |
| `AppController` | 8 dead getters/setters (98–126) | confirms the dead code called out in P4e |
| `AccountController` | `lambda$0` (163) | the label-removal branch of account deletion |

`MindmapFilter` deserves emphasis: `?q=` is the main list-filtering mechanism the
frontend uses, and **only the default `all` path is tested**. That is now P3's
highest-value item, not a nice-to-have.

### Outside the REST layer (informational — not in this plan's scope)

Flagged because they are the largest absolute gaps in the project, should you
want follow-up work:

- `AuthenticationProviderLDAP` — **0.0%**, 871 instructions, zero tests.
- `OAuth2AuthenticationSuccessHandler` — **2.1%**, 557 instructions.
- `com.wisemapping.view` — 18.8%, and `MindMapBean` is used by `MindmapController`.
- `com.wisemapping.mindmap.model` — 25.6% / 14.6% branch, 935 instructions.

---

## Priority 1 — The error contract returns 500 for every client mistake

**This is the highest-value fix in the document and should be done first.**

`GlobalExceptionHandler` registers `@ExceptionHandler(Exception.class)`. Because
it is the only handler matching Spring MVC's own request-binding exceptions, it
swallows all of them and maps them to **500 Internal Server Error**.

Measured against a running app (`@SpringBootTest`, `WebEnvironment.RANDOM_PORT`):

| Request | Correct status | Actual status |
| :--- | :--- | :--- |
| `PUT /api/restful/users/resetPassword` (no `email` param) | 400 | **500** |
| `PUT /api/restful/users/activation?code=notanumber` | 400 | **500** |
| `DELETE /api/restful/app/config` (wrong method) | 405 | **500** |
| `POST /api/restful/users/resetPasswordToken` with `{"token":"","password":"x"}` (fails `@Valid`) | 400 | **500** |
| `PUT /api/restful/account/firstname` with `Content-Type: application/pdf` | 415 | **500** |
| `GET /api/restful/doesnotexist` | 404 | **500** |
| `DELETE /api/restful/maps/batch` (no `ids` param) | 400 | **500** |

Consequences: clients cannot distinguish "I sent a bad request" from "the server
is broken"; retry/alerting logic keys on 5xx, so every malformed client call
looks like an outage; and the `ERROR`-level log line emitted by
`handleServerErrors` means ordinary client typos pollute production error logs.

### Work

1. Add explicit handlers to `GlobalExceptionHandler`, each returning `RestErrors`
   with a localized message from `messages*.properties` and logging at `DEBUG`:
   - `MethodArgumentNotValidException` → **400**, mapping `BindingResult` field
     errors into `RestErrors.fieldErrors` (the field already exists and is
     currently always empty for these).
   - `HandlerMethodValidationException` → **400** (Spring 6.1+ raises this for
     constraint violations on method parameters).
   - `MissingServletRequestParameterException` → **400**.
   - `MethodArgumentTypeMismatchException` / `TypeMismatchException` → **400**.
   - `HttpRequestMethodNotSupportedException` → **405**.
   - `HttpMediaTypeNotSupportedException` → **415**;
     `HttpMediaTypeNotAcceptableException` → **406**.
   - `NoHandlerFoundException` / `NoResourceFoundException` → **404**.
2. Add the new `MSG_KEY`s to **every** `messages_*.properties` locale file
   (`ar de en es fr hi it ja pt ru uk zh zh-CN`), English first — per the i18n
   rule in `CLAUDE.md`.
3. Keep `@ExceptionHandler(Exception.class)` as the last-resort 500, unchanged.

### Verification

New test class `wise-api/src/test/java/com/wisemapping/test/rest/RestErrorContractTest.java`
asserting the status **and** the `RestErrors` body shape for each row of the
table above. Without this test the regression comes straight back.

> Note: do **not** solve this by making `GlobalExceptionHandler` extend
> `ResponseEntityExceptionHandler`. That base class emits RFC-7807
> `ProblemDetail` bodies, which would change the response shape for every
> existing error and break the frontend's `RestErrors` parsing. Explicit
> handlers keep the existing contract.

---

## Priority 2 — Endpoints that 500 on a well-formed request

Two concrete bugs found while probing, both independent of P1.

### 2a. List endpoints only answer on the trailing-slash path

`LabelController.retrieveList` maps `value = "/"` and
`MindmapController.retrieveList` maps `value = "/"`. Result:

- `GET /api/restful/labels/` → 200
- `GET /api/restful/labels` → **500** (`Request method 'GET' is not supported`)
- `GET /api/restful/maps` → **500**

Same for `POST /api/restful/users/` vs `/users`. P1 alone turns these into a 405,
which is better but still wrong — these are valid resource paths.

**Work:** map both forms (`value = {"", "/"}`) on the collection endpoints so the
canonical no-slash path works. Do not remove the slashed form — the frontend and
the hand-written OpenAPI specs use it today.

### 2b. `DELETE /maps/batch` reports a permission error for a malformed id

```java
public void batchDelete(@RequestParam() String ids) throws WiseMappingException {
    try {
        for (final String mapId : mapsIds) {
            final Mindmap mindmap = findMindmapById(Integer.parseInt(mapId));
            ...
    } catch (Exception e) {
        throw new AccessDeniedSecurityException("Map could not be deleted. ...");
    }
}
```

`?ids=abc` throws `NumberFormatException`, gets wrapped, and the client receives
**403 "Your access permissions to this map has been revoked. Contact map owner."**
— a misleading message for what is a 400. The blanket `catch (Exception e)` also
hides genuine server faults behind a 403.

**Work:** parse and validate the id list first (throwing
`IllegalArgumentException` → 400 for non-numeric input), then let per-map
permission failures propagate as the real `AccessDeniedSecurityException` they
are. Add tests for malformed ids, an empty `ids`, and a partially-unauthorized
batch.

---

## Priority 3 — Untested endpoints

These have no test exercising them at all. One new or extended test class per
bullet; all are small.

| Endpoint | Controller | Notes |
| :--- | :--- | :--- |
| `POST /api/restful/logout` | `JwtAuthController` | Never called by any test. Cover: valid bearer token, expired token, garbage token, no `Authorization` header — all must return 200 (the method is deliberately idempotent). |
| `PUT /api/restful/oauth2/confirmaccountsync` | `OAuth2Controller` | Entire controller untested. Cover: invalid code → 400, and the happy path returning a JWT. |
| `POST /api/restful/maps/validate-note` | `MindmapController` | Cover plain text under limit, HTML note, and over-limit content (`isOverLimit`, `remainingChars`). |
| `GET /api/restful/maps/{id}` | `MindmapController` | The single-map JSON `RestMindmap` fetch. Tests cover `/metadata` and `/document/xml` but not this. |
| `GET /api/restful/maps/?q=...` filters | `MindmapController` + `MindmapFilter` | `MindmapFilter.ALL/MY_MAPS/PUBLIC/STARRED/SHARED_WITH_ME` and the `LabelFilter` fallback are never exercised through HTTP. See also 3a below. |
| `PUT /api/restful/account/password` error paths | `AccountController` | `PasswordTooShortException`, `PasswordTooLongException`, `PasswordChangeNotAllowedException` (OAuth/LDAP account) — none asserted. |
| `GET /api/restful/admin/system/health` degraded path | `AdminController` | Only the healthy path is covered. |

### 3a. `MindmapFilter.parse` silently accepts typos

An unrecognised `q` value is not rejected — it falls through to
`new LabelFilter(valueStr)`, so `?q=my_mpas` returns "maps carrying a label
literally named `my_mpas`", i.e. an empty list, with no error. Decide and
then encode in a test: either keep the fallback (and document it) or require
label filters to use an explicit prefix. **Recommendation:** keep the behaviour,
add the test, document it — changing it is a frontend-visible break.

### 3b. Null `password` on registration

`UserController.registerUser` calls `registration.getPassword().length()` before
`verify(...)` runs the validator. A registration body with no `password` field
NPEs → 500. Move the length checks after `verify(...)`, or null-check first.
Cover with a test posting `{"email":"...","firstname":"..."}`.

---

## Priority 4 — Spring Boot alignment

Ordered by value. Each is independent.

### 4a. `@EnableWebMvc` disables Spring Boot's MVC auto-configuration

`AppConfig` is annotated `@SpringBootApplication` **and** `@EnableWebMvc`. The
latter switches off `WebMvcAutoConfiguration` wholesale, which is why the app
has no default error handling, no `ProblemDetail` support, and why
`NoResourceFoundException` reaches the catch-all in P1.

This is the single most consequential deviation from Boot practice in the
codebase — and the riskiest to change, because it alters message converters,
static resource handling, and content negotiation all at once. **Do it as its own
change, after P1 and P3 are landed**, so the test suite can tell you what moved.
`AppConfig` already implements `WebMvcConfigurer`, which is the supported way to
customise MVC without `@EnableWebMvc`.

Also redundant in the same file: `@EnableWebSecurity` (already on the imported
`config.common.SecurityConfig`), and CORS configured twice — once via the
`corsConfigurationSource` bean and again via `addCorsMappings`.

### 4b. Untyped request and response bodies

`Map<String, Object>` / `Map<String, Boolean>` / raw `int` as a body means no
schema, no `@Valid`, no OpenAPI, and hand-rolled null checks:

- `MindmapController.updatePublishState(@RequestBody Map<String, Boolean>)`
- `MindmapController.updateLabel(@PathVariable int id, @RequestBody int lid)`
- `AdminController.updateUserSuspension(@RequestBody Map<String, Object>)`
- `AdminController.updateMapSpamStatus(@RequestBody Map<String, Boolean>)`
- `AdminController.getSystemInfo()` / `getBuildInfo()` / `getSystemHealth()` all
  return `Map<String, Object>`

**Work:** add DTOs under `rest/model/` (e.g. `RestPublishRequest`,
`RestSuspensionRequest`, `RestSystemInfo`) and annotate the bodies `@Valid`. Keep
the JSON field names byte-identical so the frontend is unaffected — assert that
in the tests. Pairs naturally with P1, which makes `@Valid` failures return 400.

### 4c. `GET /api/restful/maps/` is unpaginated and silently truncates

`retrieveList` loads every map for the user, and when the count exceeds
`app.mindmap.list.max-size` (default 500) it **drops the remainder** and logs a
warning. The client gets 200 with a short list and no indication data is missing.
`AdminController` already has `PaginatedResponse<T>` for exactly this.

**Work:** either paginate the user-facing list with `PaginatedResponse` (frontend
change required — coordinate with `wisemapping-frontend`), or, as a smaller
first step, surface the truncation in the response so clients can detect it.

### 4d. Replace `@RequestMapping(method = ...)` with the composed annotations

All 55-odd mappings use the long form. `@GetMapping` / `@PostMapping` /
`@PutMapping` / `@DeleteMapping` are the idiomatic Boot style and read better.
Purely mechanical, zero behaviour change, large diff — land it **alone**, not
mixed with a functional change.

### 4e. Small code-quality fixes

- `MindmapController`: `private final Logger logger` should be
  `private static final` per the `CLAUDE.md` logging convention.
- `MindmapController:706` `logger.debug("Update starred:" + value)` and
  `UserController:154` `logger.debug("Activating account with code: " + code)`
  use string concatenation; the convention requires parameterised messages.
- `MindmapController.isShowcaseModeEnabled` constructs `new ObjectMapper()` on
  every call — hoist to a static field or inject the context's bean.
- `MindmapController.validateNoteContent` catches `Exception` and returns a
  **fake success** response (`new NoteValidationResponse(0, 0, false, max, false, 0.0)`).
  A real failure should surface as an error, not as "your note is fine".
- `AppController` carries eight dead public getters/setters over `@Value` fields
  (`getRegistrationEnabled`, `setCaptchaSiteKey`, …) — unreferenced anywhere, and
  mutable state on a singleton controller. Delete them.
- `isAdmin(String email)` — comparing against `${app.admin.user}` — is duplicated
  verbatim in `AccountController` and `AdminController`, and `app.admin.user` is
  read in five classes. Extract one helper (e.g. on `UserService` or a small
  `AdminIdentityService`).
- `AdminController` uses fully-qualified type names inline throughout
  (`com.wisemapping.rest.model.AdminRestUser`, …) instead of imports.
- `AccountController.deleteUser` uses inline `java.util.Set` /
  `java.util.HashSet` instead of imports.

### 4f. Constructor injection — ❌ **decided: not doing it**

Spring Boot practice is constructor injection, but `CLAUDE.md` records
`@Autowired` field injection as the house style and **the decision is to keep
`@Autowired`**. Not revisiting; new code should match the surrounding files.

---

## Priority 5 — Test infrastructure

### 5a. Add JaCoCo so coverage is a number, not a guess — ✅ **DONE**

`jacoco-maven-plugin` 0.8.13 is wired into `wise-api/pom.xml`:

- `prepare-agent` publishes its JVM argument as the `jacocoArgLine` property,
  which the existing surefire `<argLine>` consumes via `@{jacocoArgLine}`. The
  property is declared empty in `<properties>` so the argLine still resolves
  under `-Djacoco.skip=true` (verified).
- `report` is bound to the `test` phase, so plain `mvn test` emits
  `target/jacoco.exec` plus `target/site/jacoco/{index.html,jacoco.xml}`.
- Entities, DTOs and JAXB holders (`model/**`, `rest/model/**`,
  `mindmap/jaxb/**`) are excluded — counting accessors inflates the number
  without saying anything.
- **Reporting only. No `check` goal, no threshold** — the build cannot fail on
  coverage. Consider a gate later, set at or just below the measured baseline.

Suite re-verified with the agent attached: 577 tests, 0 failures, 0 errors,
1 skipped — unchanged. Baseline numbers are recorded above.

### 5b. The project ships a hand-rolled `TestRestTemplate`

`wise-api/src/test/java/org/springframework/boot/test/web/client/TestRestTemplate.java`
is a local class placed in a **Spring Framework package** in order to shadow the
framework's own type (presumably a Spring Boot 4 migration workaround). Tests
instantiate it manually with `new TestRestTemplate("http://localhost:" + port)`
rather than injecting the managed bean.

This is fragile: the shadowing silently stops working if the real class
reappears on the classpath ahead of it, and the shim reimplements only the
methods the tests happen to use. **Work:** determine what the Boot 4 replacement
is (`RestTestClient` / `RestClient`-based test support), migrate to it, and
delete the shadow class.

### 5c. Dead and disabled tests

- `AdminControllerTest.java.broken` (465 lines) — delete it or fix and re-enable.
  A `.broken` file in the source tree is invisible to CI and rots.
- `RestMindmapControllerTest.fetchMapMetadataWithAllExtendedFields` is
  `@Disabled` with no recorded reason. Fix it or delete it with a note.

### 5d. Test documentation is out of date

`src/test/java/com/wisemapping/test/rest/README.md` states "**No
`@DirtiesContext`** — context is not reloaded, improving test performance", but
all ten REST test classes carry `@DirtiesContext(AFTER_CLASS)`. That means ten
full context reloads per run. Either the doc is wrong or the annotations are —
decide which, fix the other.

### 5e. Naming and package conventions

`CLAUDE.md` says a test mirrors its SUT (`FooService` → `FooServiceTest`). The
REST tests live in `com.wisemapping.test.rest` (SUT is `com.wisemapping.rest`)
and names are inconsistent: `AdminControllerTest` vs
`RestAccountControllerTest`. Low priority, but worth settling before adding the
new classes in P1/P3 so they land in the right place.

---

## Priority 6 — Generate the OpenAPI spec instead of hand-maintaining it

`doc/api-documentation/backend/openapi-specs/` holds three hand-written YAML
files (~2,000 lines). `openapi.yaml` documents 10 paths; the code exposes ~55.
It is already badly out of date and there is nothing keeping it honest.

**Work:** add `springdoc-openapi-starter-webmvc-ui`, annotate the controllers,
serve the spec from the running app, and either delete the hand-written files or
reduce them to a generated artifact. Blocked on **4a** — springdoc needs Boot's
MVC auto-configuration, which `@EnableWebMvc` currently disables.

---

## Suggested order

0. ~~**P5a** — JaCoCo~~ ✅ done; baseline recorded above.
1. **P1** — error contract + `RestErrorContractTest`. Biggest win, fully additive, no client-visible change to existing success or error shapes. Coverage says 17 of 26 handlers are unexercised, so this raises `config` branch coverage sharply as a side effect.
2. **P2** — the two 500-on-valid-request bugs.
3. **P3** — untested endpoints. Start with **`MindmapFilter`** (4 of 5 filters at 0%, 12.5% branch) and `JwtAuthController.logout` (42% class, 14% branch); 3b is a real NPE.
5. **P5b/c/d** — test infrastructure cleanup.
6. **P4b, P4e** — DTOs and code-quality fixes.
7. **P4a** — remove `@EnableWebMvc`, alone, with the full suite as the safety net.
8. **P6** — generated OpenAPI (needs 4a).
9. **P4c, P4d** — pagination (needs frontend coordination) and the mechanical annotation sweep.
