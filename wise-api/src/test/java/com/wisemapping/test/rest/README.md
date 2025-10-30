# WiseMapping API REST Tests

This directory contains integration tests for the WiseMapping REST API.

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

## Test Configuration

### Password Requirements
All user passwords must meet the following requirements:
- Minimum length: **8 characters** (defined by `Account.MIN_PASSWORD_LENGTH_SIZE`)
- Maximum length: **40 characters** (defined by `Account.MAX_PASSWORD_LENGTH_SIZE`)

### Test Data Setup
- Test users are initialized via SQL in `src/main/resources/data-hsqldb.sql`
- Passwords are stored as SHA-1 hashes with the `ENC:` prefix
- Dynamic test users are created during test execution using `TestDataManager`

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

## Test Classes

- **AdminControllerTest**: Tests for admin endpoints (user management, maps, etc.)
- **AdminSystemControllerTest**: Tests for system admin endpoints (info, health)
- **RestAccountControllerTest**: Tests for account management
- **RestLabelControllerTest**: Tests for label operations
- **RestMindmapControllerTest**: Tests for mindmap CRUD operations
- **RestUserControllerTest**: Tests for user registration and authentication
- **TestDataManager**: Utility class for creating test data

## Running Tests

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=RestUserControllerTest

# Run with Java 21 (required for Mockito compatibility)
JAVA_HOME=/path/to/java21 mvn test
```

## Important Notes

1. **Password Changes**: If you change test passwords, you must also update:
   - The constant in the test class (e.g., `ADMIN_PASSWORD`, `REGULAR_PASSWORD`)
   - The SHA-1 hash in `data-hsqldb.sql`
   - The password in `TestDataManager.java` if applicable

2. **Mockito Compatibility**: Tests require Java 21 with `-XX:+EnableDynamicAgentLoading` JVM flag for Mockito to work on macOS 26.0.1+

3. **Test Isolation**: Tests ensure proper isolation through:
   - `RANDOM_PORT` web environment for network isolation
   - `@ActiveProfiles("test")` for consistent test configuration
   - In-memory HSQLDB database (fresh per test run)
   - `TestDataManager` for data cleanup between tests
   - `@Transactional` on service tests for entity session management
   - **No `@DirtiesContext`** - context is not reloaded, improving test performance

4. **Log Level**: Test logging is configured to WARNING level in `logback-test.xml` to reduce noise

