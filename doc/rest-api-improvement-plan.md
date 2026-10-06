# REST API Improvement Plan

Scope: the `com.wisemapping.rest` HTTP surface, its error contract, and its test
coverage — plus the `com.wisemapping.dao` DB-access layer underneath it. Each
item below is self-contained and can be landed on its own. Work them **one at a
time**, running the full suite after each.

**Keep this file current.** It is both the backlog and the record of what has
already shipped. When you finish an item, move it in the status table and tick
the box in its own section.

---

## Status

Branch: `improve-rest-api-coverage` (off `develop`).

| # | Item | Status |
| :--- | :--- | :--- |
| P5a | JaCoCo coverage reporting | ✅ **done** — `3c31908c` |
| P5f | `UserManagerImpl` DAO integration tests | ✅ **done** — `bf7536b8`, 19 tests |
| P5l | DAO test naming normalized | ✅ **done** — `9ae9c180` |
| P5g | `InactiveMindmapManagerImpl` DAO integration tests | ✅ **done** — `3025c8bb`, 17 tests |
| P5h | `MindmapManagerImpl` spam-user threshold queries | ✅ **done** — `f7ab839a`, 12 tests |
| P5i | `MindmapManagerImpl` spam-ratio / spam-type queries | ✅ **done** — `af881030`, 19 tests |
| P5j | `MindmapManagerImpl` public-mindmap queries | ✅ **done** — `2c0b2829`, 10 tests |
| P5k | `MindmapManagerImpl` admin listing / search / history | ✅ **done** — `4dbb6801`, 23 tests |
| P1 | Error contract returns 500 for every client mistake | ✅ **done** — `d233fdb6` |
| P2a | List endpoints only answer on the trailing-slash path | ✅ **done** — `911c81f3` |
| P2b | `DELETE /maps/batch` reports 403 for a malformed id | ✅ **done** — `128c7c7f` |
| **D1** | `?filterLocked=` is a live REST param that does nothing | ✅ **backend done** — `f6020d5e`; frontend dropdown still to remove |
| **D2** | `…PublicSpamMapsByType` throws on every input | ⬜ **new — found by these tests** |
| **D3** | `findPublicMindmaps` / `countAllPublicMindmaps` disagree on suspended creators | ⬜ **new — found by these tests** |
| **D4** | `findUsersWithMinimumMapsAndSpam` off-by-one (`>` not `>=`) | ⬜ **new — found by these tests** |
| **D5** | 11 DAO methods have no callers — delete rather than test | ⬜ **new — found by these tests** |
| **D6** | `/maps/{id}/metadata` omitted `starred` and `locked` | ✅ **done** — `543a09e8` |
| **W1** | 282 warnings per run buried real signal; 2 tests ran on prod config | ✅ **done** — `4b9b228a` |
| **F1** | Frontend treats HTTP **405** as session expiry → spurious logout | ⬜ **needs a decision before release** |
| **F2** | Frontend "Locked" admin dropdown + mock client mask D1 | ⬜ frontend repo |
| **F3** | `ehcache` calls terminally-deprecated `sun.misc.Unsafe` | ⬜ dependency upgrade |
| P3 | Untested REST endpoints (`MindmapFilter` now 100% branch) | ✅ **done** — `bfefd82f`, `6b5b6846`, `7d7cda20` |
| P3b | Null `password` on registration NPEs → 500 | ✅ **done** — `e85149e0` |
| P4a | `@EnableWebMvc` disables Boot MVC auto-configuration | ⬜ not started |
| P4b | Untyped `Map<String,Object>` request/response bodies | ⬜ not started |
| P4c | `GET /maps/` unpaginated, silently truncates at 500 | ⬜ not started |
| P4d | `@RequestMapping(method=…)` → `@GetMapping` etc. | ⬜ not started |
| P4e | Small code-quality fixes | ✅ **done** — `b9738102`, `a0ebe5d0` |
| P4f | Constructor injection | ❌ **declined** — keeping `@Autowired` |
| P5b | Hand-rolled `TestRestTemplate` shadow class | ⬜ not started |
| P5c | `AdminControllerTest.java.broken`, one `@Disabled` test | ✅ **done** — `ed962f6f`, `3f1004d0` |
| P5d | Test README contradicts `@DirtiesContext` usage | ✅ **done** — `e40d3a23` |
| P5e | REST test naming / package conventions | ⬜ not started |
| P6 | Generate the OpenAPI spec (blocked on P4a) | ⬜ not started |

---

## Runbook

All commands from the repository root.

```sh
# Full suite. Also writes the coverage report (report is bound to the test phase).
mvn -f wise-api/pom.xml test

# Full suite without the coverage agent (faster; verifies the argLine fallback).
mvn -f wise-api/pom.xml test -Djacoco.skip=true

# One test class, no coverage — the normal inner loop while writing tests.
mvn -f wise-api/pom.xml test -Dtest=UserManagerImplIntegrationTest \
    -DfailIfNoSpecifiedTests=false -Djacoco.skip=true

# One test method.
mvn -f wise-api/pom.xml test -Dtest='UserManagerImplIntegrationTest#findLastLoginDateReturnsMostRecent' \
    -DfailIfNoSpecifiedTests=false -Djacoco.skip=true

# All the DAO integration tests.
mvn -f wise-api/pom.xml test -Dtest='*IntegrationTest' \
    -DfailIfNoSpecifiedTests=false -Djacoco.skip=true
```

### Reading the results

`mvn -q` suppresses the surefire summary, so **do not trust a quiet console** —
read the XML, which is the authoritative count:

```sh
python3 - <<'EOF'
import glob, xml.etree.ElementTree as ET
t = f = e = s = 0
for p in sorted(glob.glob('wise-api/target/surefire-reports/*.xml')):
    r = ET.parse(p).getroot()
    t += int(r.get('tests')); f += int(r.get('failures'))
    e += int(r.get('errors')); s += int(r.get('skipped'))
print(f"tests={t} failures={f} errors={e} skipped={s}")
EOF
```

### Checking for errors and warnings, not just failures

A green suite can still be emitting stack traces and warnings. Audit both:

```sh
# The application's own log output during the run (NOT test failures)
grep -cE '^[0-9-]{10}T.*ERROR' run.log     # must stay 0
grep -cE 'WARN' run.log                     # currently 43

# Group the warnings so a repeated one cannot hide the rest
grep -E 'WARN' run.log \
  | sed -E 's/^.*WARN[^]]*\] *//; s/^[^:]*: *//; s/[0-9]+/N/g' \
  | cut -c1-82 | sort | uniq -c | sort -rn
```

Grouping matters: before `4b9b228a` the run emitted 282 warnings, **203 of them
three messages repeated once per Spring context**, which made every real warning
invisible. It is now 43, and the remainder are either benign SQL `01003`/`02000`
aggregate warnings from the DAO tests or deliberately provoked by tests (spam
detection, HTML validation, the 404/406 probes).

Three `sun.misc.Unsafe` deprecation warnings come from **ehcache** and will break
on a future JDK — that is a dependency upgrade (**F3**), not something fixable
here.

### Coverage report

```sh
# HTML, for browsing
open wise-api/target/site/jacoco/index.html

# Machine-readable, for the per-package / per-class / per-method breakdowns
# quoted throughout this document
wise-api/target/site/jacoco/jacoco.xml
```

To reproduce the tables in this document, summarise `jacoco.xml` by package,
by class, or by method — the counters are nested
`<package>` → `<class>` → `<method>` → `<counter type="INSTRUCTION|BRANCH|LINE|METHOD">`
each carrying `missed` and `covered` attributes.

---

## Baseline (measured 2026-10-05, commit `e5d57f18`)

| Fact | Value |
| :--- | :--- |
| Full suite | baseline **577** → now **733 tests, 0 failures, 0 errors, 0 skipped** |
| REST test classes | 10 (`wise-api/src/test/java/com/wisemapping/test/rest/`), ~5.6k LOC |
| Controller LOC | 3,012 across 8 controllers (`AdminController` 1,005, `MindmapController` 1,042) |
| Coverage tooling | JaCoCo 0.8.13, wired in P5a ✅ |
| **Project coverage** | 54.1% → **60.1% instruction**, 45.2% → **47.8% branch**, 55.7% → **60.5% method** |
| **`com.wisemapping.rest`** | **66.9% instruction, 52.2% branch, 74.8% method** at the P5a baseline; P1/P3 have since landed — re-measure |
| **`com.wisemapping.dao`** | 46.7% → **85.1% instruction**, 39.9% → **59.0% branch**, 50.0% → **96.2% method** |
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

## Cross-repo contract notes (`wisemapping-frontend`)

`packages/webapp` is the **only** frontend package that calls the REST API —
`editor`, `mindplot` and `web2d` contain no `api/restful` reference. So any
contract check only has to cover `webapp`.

### F1 — the frontend treats HTTP 405 as session expiry ⚠️ release blocker

`packages/webapp/src/classes/client/rest-client/index.ts` (~line 51):

```ts
if (status === 405 || status === 403 || (status === 401 && !url.endsWith('/authenticate'))) {
  this.sessionExpired();
}
```

A 405 **logs the user out**. P1 now returns 405 where the API previously returned
500, so any wrong-method request goes from "generic error" to a forced logout.

405 is the correct status and the frontend mapping is the bug — most likely a
leftover from the `wise-webapp` form-login era, when a session timeout produced a
redirect that surfaced as 405. **Either drop `status === 405` from that check, or
release the two repos together.** This is the one item in this document that can
regress user-visible behaviour on deploy.

The rest of P1 is safe for the frontend, and in fact better: it reads
`globalErrors` and `fieldErrors` off `RestErrors`, which is exactly the shape
preserved by deliberately not adopting RFC-7807 `ProblemDetail` — and
`fieldErrors` is now populated for `@Valid` failures where it was always empty.

### F2 — the admin "Locked" filter is dead on both sides, and the mock hides it

D1 is worse than the backend alone suggests:

- `packages/webapp/src/components/admin-console/maps-page/` renders a **Locked**
  dropdown (All / Locked / Unlocked) wired to `filterLocked`.
- `admin-client` sends it; `AdminRestMap` has **no locked field at all**, so
  `mapData.isLocked` is always `undefined` and the table column is broken too.
- `mock-admin-client` **does** implement the filter
  (`map.isLocked === params.filterLocked`), so the feature works in dev against
  the mock and silently fails against the real API. That is why it survived.

Locks are intentionally in-memory in `LockManagerImpl` and deliberately not
persisted, so this can never be a SQL predicate; implementing it would mean
post-filtering results against `LockManager`, which cannot produce correct
pagination totals without scanning every row. Removing the backend parameter was
behaviour-preserving (Spring ignores unknown query params). **Remaining work is
frontend-side:** remove the dropdown, the `admin-client` field, and the mock
implementation.

### Additive changes, safe to deploy alone

- **D6** — `/maps/{id}/metadata` now serializes `starred` and `locked`. Purely
  additive; the frontend was silently not receiving either.
- **P2a** — collection endpoints now answer on the no-slash path as well as the
  slashed one. The slashed form is unchanged.

### Confirmed non-issue: PDF export

There is no PDF code in the backend — no library in `pom.xml`, no export
plumbing, no binary-producing endpoint, nothing in the specs. The old subsystem
went out with the deleted `wise-webapp` module (`ExportController`, `Exporter`,
`ExporterFactory`, `ExportFormat`, `XSLTExporter` are all in the deleted-file
history). PDF export still exists but is **entirely client-side**, in
`packages/mindplot/src/components/export/PDFExporter.ts` via `jspdf`, with no API
call. The only `application/pdf` strings in this repo are in
`RestErrorContractTest`, where it is an arbitrary *unsupported* media type used to
provoke 415/406. Nothing to clean up, and no orphaned client call.

---

## Defects found by the DAO integration tests

Writing P5f–P5l surfaced five problems that no amount of reading would have
found, because four of them only manifest when the query actually reaches a
database. Each is **pinned by a passing test that asserts current behaviour**,
so fixing one will fail its test loudly — that is intentional. None of the
production code was changed.

### D1. `?filterLocked=` is a live REST parameter that silently does nothing

`AdminController:607` declares `@RequestParam("filterLocked") Boolean
filterLocked` and threads it through `mindmapService` into
`MindmapManagerImpl.searchMindmaps` / `countAllMindmaps`. It appears in **six**
DAO signatures and is bound into a predicate in **none** of them — there is an
in-source comment conceding it is unimplemented.

So `GET /api/restful/admin/maps?filterLocked=true` returns **every** map with a
200 OK. An admin cannot tell the filter did nothing. This is the most
user-visible defect in this document.

Pinned by `MindmapManagerAdminQueryIntegrationTest.searchMindmapsIgnoresTheLockedFilter`
(`true`/`false`/`null` all return the identical set).

**Fix:** either implement it against the lock manager, or remove the parameter
from the controller and the six DAO signatures. Leaving a parameter that lies
is the one option to rule out.

### D2. `findUsersWithPublicSpamMapsByType` throws on every possible input

`MindmapManagerImpl:951` (mirrored at `:1012`) binds `String` values into
`s.spamTypeCode IN (:spamType0, …)`, but `MindmapSpamInfo.spamTypeCode` is a
`SpamStrategyType` behind `@Convert(converter = SpamStrategyTypeConverter.class)`
— an `AttributeConverter<SpamStrategyType, Character>`. Hibernate rejects the
binding before issuing any SQL:

```
org.hibernate.query.QueryArgumentException: Argument to parameter named
'spamType0' has an incompatible type (argument [C] is not assignable to
com.wisemapping.model.SpamStrategyType)
```

It fails for **all three** plausible spellings: the single-char code `"C"`, the
strategy name `"ContactInfo"` that the interface javadoc actually recommends,
and the enum name `"CONTACT_INFO"`. This is parameter validation, not SQL, so
PostgreSQL behaves identically — it is **not** an HSQLDB artifact.

Severity is limited by reachability: the only caller is
`SpamUserSuspensionService.suspendUsersWithPublicSpamMapsByType`, which itself
has **no callers**, so nothing in production invokes it today. It is a loaded
gun, not a live outage.

**Fix:** change the parameter to `SpamStrategyType[]`, or map the codes through
`SpamStrategyType.fromCode` before binding.

### D3. `findPublicMindmaps` and `countAllPublicMindmaps` disagree

The two named queries on the `Mindmap` entity apply different filters:

```java
"Mindmap.findPublicMindmaps"    → SELECT m FROM Mindmap m JOIN m.creator c
                                  WHERE m.isPublic = true AND c.suspended = false
"Mindmap.countAllPublicMindmaps"→ SELECT COUNT(m) FROM Mindmap m
                                  WHERE m.isPublic = true
```

The finder excludes suspended creators; the counter does not, and neither does
`findAllPublicMindmaps`. Pairing the count with the finder for pagination
over-reports the total and yields short or empty pages.

Currently latent — none of the three has a caller (see D5).

**Fix:** decide which filter is intended and apply it to all three. Pinned by
`MindmapManagerPublicMapQueryIntegrationTest.findPublicMindmapsExcludesSuspendedCreators`.

### D4. `findUsersWithMinimumMapsAndSpam` is off by one

`HAVING COUNT(m.id) > :minTotalMaps` uses `>` where the parameter name and
every sibling query use `>=`. A user with exactly `minTotalMaps` public maps is
**excluded**. `countUsersWithMinimumMapsAndSpam` repeats the same `>`, so find
and count stay consistent with each other — only the boundary is wrong.

Currently latent — no callers (see D5). Pinned by
`findUsersWithMinimumMapsAndSpamUsesExclusiveTotalMapsBound`.

### D5. Eleven DAO methods have no callers at all

This reframes the whole coverage gap: these were at 0% **because nothing calls
them**, not because testing was overlooked.

`findPublicMindmaps`, `findAllPublicMindmaps`, `countAllPublicMindmaps`,
`findAllPublicMindmapsSince`, `countAllPublicMindmapsSince`,
`findUsersWithSpamMindmaps`, `findUsersWithSpamMindapsCursor`,
`findUsersWithHighSpamRatio`, `countUsersWithHighSpamRatio`,
`findUsersWithMinimumMapsAndSpam`, `findLastLoginDate`

Every one is hand-written JPQL carrying maintenance cost and implying a
capability the application does not have. `findLastLoginDate` is the clearest
case — `InactiveUserService` still carries a comment referring to it, so it
looks refactored-away rather than never-wired.

**For these the right fix is deletion, not tests.** The tests now documenting
them are cheap insurance if any get wired up, but the coverage number should
not be what motivates keeping them. Note that D3 and D4 both live in this dead
set, which lowers their priority considerably.

### Non-defects worth knowing

- **HSQLDB truncates ratio-threshold precision.** `COUNT(…) * 1.0 / COUNT(m.id)`
  is a one-decimal `DECIMAL` on HSQLDB, which coerces the bound threshold to
  that scale: a 0.5 ratio matches thresholds 0.4 through 0.55 but not 0.6.
  PostgreSQL's numeric division has far higher scale, so production likely does
  not truncate — but **HSQLDB-based tests cannot assert sub-0.1 threshold
  granularity**. The ratio assertions deliberately stay on a 0.1 grid.
- **`cb.isNull(spamJoin)`** in `getAllMindmaps(Boolean filterSpam, …)` calls
  `isNull` on a `Join` path rather than a scalar attribute. Hibernate 6 resolves
  it to the join's FK column, which happens to give the intended "no spam-info
  row" semantics — correct, but incidental and fragile across upgrades. The
  companion `countAllMindmaps` writes the same predicate differently, so the two
  can drift apart.
- **`getAllMindmaps(Boolean filterSpam, …)`** issues both a
  `fetch("spamInfo", LEFT)` and a separate `join("spamInfo", LEFT)` — two joins
  on one table, redundant SQL.
- **`countUsersWithSpamMindaps`** uses an `IN (subquery)` shape where the `find`
  variants use `HAVING`. They agreed on every fixture built, so the extra outer
  predicates are harmless but non-obvious.
- **`InactiveMindmapManagerImpl`** annotates parameters with
  `jakarta.validation.constraints.@NotNull`; `CLAUDE.md` mandates
  `org.jetbrains.annotations`. Cosmetic — belongs in P4e.
- **`findUsersWithSpamMindaps`** is missing an "m" ("Mindaps") across the
  interface and every call site. Cosmetic — belongs in P4e.

### Known coverage gap

`removeCollaboration` is only partially covered (the `null` branch). Its
`@Transactional(propagation = REQUIRES_NEW)` means a fixture written inside the
rolled-back test transaction is invisible to the genuinely new transaction, and
the alternative — deleting a committed seed row — would leak into sibling test
classes sharing the in-memory database. Covering it properly needs a different
transaction strategy than the rest of these tests use.

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

### 5f–5k. Test the DAO layer directly, not through the API

**Why this is its own line of work.** `com.wisemapping.dao` is 4,436
instructions at **46.7% instruction / 39.9% branch**, and what coverage it has
is incidental — picked up as a side effect of REST calls. The layer is almost
entirely hand-written JPQL strings, named-query references and Criteria API
construction, i.e. code where a typo, a wrong join, or an unresolvable named
query is a **runtime** failure that compiles perfectly well.

The pre-existing `MindmapManagerImplTest` mocks the `EntityManager`, so it
asserts Criteria API *call sequences*. That can never catch a malformed query —
the mock will happily return whatever it was told to. These tests instead run
every query through the real in-memory HSQLDB schema, bypassing the REST layer.

**The established pattern** (follow it for any new DAO test):

```java
@SpringBootTest(classes = {AppConfig.class})
@ActiveProfiles("test")
@Transactional                      // each test rolls back
class FooManagerImplIntegrationTest {
    @Autowired private FooManager fooManager;        // inject the INTERFACE
    @PersistenceContext private EntityManager entityManager;   // for fixtures
}
```

Fixture builders live in `wise-api/src/test/java/com/wisemapping/dao/DaoTestSupport.java`
(`persistAccount`, `persistMindmap`, `markAsSpam`, `markAsScannedClean`,
`persistHistory`, `persistLogin`, `persistInactiveMindmap`, `daysAgo`,
`uniqueEmail`).

**The trap to know about:** the `data-hsqldb.sql` seed rows are *always*
present, and some seed maps are public. Any assertion on an absolute row count
or an exact result-set size is latent flakiness. Capture a baseline and assert
the delta, or filter the result set to your own fixture ids.

| Item | Target | What it covers |
| :--- | :--- | :--- |
| 5f ✅ | `UserManagerImpl` | listing + paging, search + count agreement, Facebook-token `IN` lookup, the three-way inactivity predicate, suspend/unsuspend incl. the INACTIVITY restore path, `MAX(loginDate)` incl. the empty-set → null case, and collaborator→account promotion |
| 5g ✅ | `InactiveMindmapManagerImpl` | `findCreatedBefore`, `countCreatedBefore`, `countAllInactiveMindmaps`, `deleteOlderThan` (CriteriaDelete), `findAll` ordering, inclusive cutoff boundaries |
| 5h ✅ | `MindmapManagerImpl` spam-user queries | the `findUsersWithSpamMindmaps` / `findUsersWithSpamMindaps` family incl. the cursor variant, `GROUP BY … HAVING >= threshold` boundaries, public-and-spam-only filtering |
| 5i ✅ | `MindmapManagerImpl` spam-ratio queries | `COUNT(CASE WHEN …)` ratio thresholds either side of the boundary, `…MinimumMapsAndSpam`, `…HighPublicSpamRatio`, `…AnySpamMaps`, and the dynamic `IN` clause in `…PublicSpamMapsByType` incl. its empty-array early return |
| 5j ✅ | `MindmapManagerImpl` public-map queries | the `Mindmap.findPublicMindmaps` / `countAllPublicMindmaps` **named queries** actually executing, `…Since` cutoffs, and all three `spamDetectionVersion` states in `…NeedingSpamDetection` (no row / stale / current) |
| 5k ✅ | `MindmapManagerImpl` admin queries | `getAllMindmaps` overloads incl. the `filterSpam=false` arm that must also match maps with no spam-info row, `searchMindmaps` title-or-description case-insensitive matching, the deliberately-unimplemented `filterLocked` parameter, `getMindmapLastModificationTime`, and `removeExcessHistoryByMindmapId` keeping the N *most recent* rows |

**Outcome:** 100 new tests, suite 577 → 677, and
`com.wisemapping.dao` went 46.7% → **85.1% instruction**, 39.9% → **59.0%
branch**, 50.0% → **96.2% method**. Five defects fell out of the exercise — see
*Defects found by the DAO integration tests* above.

Still **not** covered, and worth a follow-up:

- `UserManagerImpl.getUsersWithFilters` / `countUsersWithFilters`
- `MindmapManagerImpl.getAllMindmaps` / `searchMindmaps` / `countMindmapsBySearch`
  overloads taking `dateFilter` and the 4-arg `filterSpam` form
- `removeCollaboration` beyond its `null` branch (see the known gap above)

The residual uncovered instructions in `InactiveMindmapManagerImpl` are almost
entirely `assert x != null` guards, which are inert without `-ea` — treat that
class as done.

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
