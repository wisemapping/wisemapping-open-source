# Changelog

All notable changes to the Wisemapping Frontend project are documented in this file.

## [Unreleased]

### 🔧 Backend (wisemapping-open-source)

#### Security & Privacy
- **OAuth Token Encryption**: Added encryption for OAuth tokens to prevent token leakage
- **PII Removal from Logs**: Stripped personally identifiable information (email addresses, names) from server logs across the authentication and user management stack
- **Email → User ID in Logs**: Replaced email addresses in log statements with internal user IDs to prevent PII exposure in log aggregation systems
- **Metrics PII Cleanup**: Removed PII and unnecessary metadata from all metrics tracking events
- **Metrics Disabled by Default**: Disabled metrics collection by default; operators must explicitly opt in
- **Reset Password Endpoint Protection**: Strengthened authorization checks on the reset-password and logout endpoints to prevent unauthenticated access
- **Login Migration to User ID**: Refactored login session handling to reference internal user IDs rather than email addresses

#### Features
- **Robust Reset Password**: Replaced the previous reset-password flow with a secure link-based mechanism — a time-limited signed token is emailed to the user; clicking the link opens a dedicated reset form, eliminating the possibility of replay or enumeration attacks

#### Maintenance
- **Dependency Bump**: Updated backend dependencies to latest stable versions
- **Dead Code Removal**: Cleaned up unused code paths and legacy tracking utilities

### 🎨 Frontend (wisemapping-frontend)

#### Features
- **Icon Picker Enhancements**: Added full-text search and a "Frequently Used" section to the icon picker gallery for faster icon discovery
- **AI Icons**: Added a new set of AI-themed icons to the icon panel
- **Remember Position**: Added support for remembering the last scroll/zoom position in the editor canvas

#### Bug Fixes
- **SVG Icon Rendering**: Fixed an issue where SVG icons in the icon gallery were not rendering all paths correctly
- **CSS Border Conflict**: Fixed a form-fieldset border-reset in `theme/index.ts` (changed `& fieldset` → `& fieldset:not(.MuiOutlinedInput-notchedOutline)`) that collided with MUI's internal notched-outline fieldset, eliminating an injection-order race condition
- **PII Log Issues**: Fixed several places where user email addresses were leaked into client-side logs

#### Testing
- **End-to-End Tests**: Extended webapp Cypress test suite with new user-flow scenarios
- **Reset-Password Flow Test**: Added an end-to-end test covering the full forgot-password → email link → reset form flow
- **Registration Layout**: Improved the registration page layout and fixed related Cypress tests

#### Translations
- Added missing i18n keys for consent, registration, and reset-password flows

#### Maintenance
- Excluded golden test files from Prettier formatting
- Updated frontend dependencies

---

## [6.0.7] - April, 2026

### 🚀 CI/CD & Release Management

#### Release Tagging
- **Automated Git Tagging**: Publishing a Docker App image via `workflow_dispatch` now automatically creates and pushes a git tag on both `wisemapping-open-source` and `wisemapping-frontend` repositories, ensuring every published image is traceable to an exact source snapshot
- **Cross-Repository Token**: Frontend tagging is performed using a scoped fine-grained PAT (`WISEMAPPING_FRONTEND_TOKEN`) with `contents: write` permission — required even within the same org, as the default `GITHUB_TOKEN` is restricted to the triggering repository
- **Release Tag in Build**: The `workflow_dispatch` tag input is forwarded to Maven as `-Dbuild.number`, so the release version is baked into the JAR at build time and visible in the Spring Boot startup banner

#### Spring Boot Startup Banner
- **Version Block**: Replaced the plain-text banner with a boxed format displaying the release tag, build date, Git branch and commit, and Spring Boot version at every startup
- **Removed POM Version**: The Maven artifact version (`x.y.z-SNAPSHOT`) is no longer shown in the banner — the release tag is the single source of version truth, defaulting to `unknown` when no tag was provided at build time

#### CORS
- **Eliminated `HandlerMappingIntrospector` Warning**: Spring Security's `.cors(Customizer.withDefaults())` was silently falling back to `HandlerMappingIntrospector` on every request because no `CorsConfigurationSource` bean existed. Added an explicit `CorsConfigurationSource` bean that mirrors the existing MVC CORS mapping, resolving the `WARN` log

### 🔧 Backend (wisemapping-open-source)

#### Bug Fixes
- **[#62](https://github.com/wisemapping/wisemapping-open-source/issues/62)**: Fixed full-stack Dockerfile missing a required layer, causing container startup failures in certain deployment configurations
- **[#63](https://github.com/wisemapping/wisemapping-open-source/issues/63)**: Strengthened `GlobalExceptionHandler` with consolidated helper methods for identifying and classifying expected exceptions, reducing noise in error logs
- **[#64](https://github.com/wisemapping/wisemapping-open-source/issues/64)**: Fixed CORS `HandlerMappingIntrospector` warning and improved the CI/CD release pipeline with automated tagging and build number propagation
- **[#65](https://github.com/wisemapping/wisemapping-open-source/issues/65)**: Fixed exception thrown during mindmap save caused by an incorrect path configuration in the Dockerfile
- **Logger Configuration**: Corrected misconfigured logger settings in `application.yml` that were producing errors at startup
- **Admin Role Check**: Fixed role prefix comparison using `startsWith` across `AccountController`, `AdminController`, `UserDetailsService`, and `MindmapServiceImpl` — previously failing for users with the `ROLE_` prefix
- **Admin Email**: Corrected admin contact email from `admin@wisemapping.com` to `admin@wisemapping.org` in documentation and README
- **User Registration Privacy**: Added mandatory privacy policy confirmation field to user registration (`RestUserRegistration`); `UserController` now enforces its presence before account creation
- **JWT Cookie Overflow**: Added an error-level log warning when a generated JWT token exceeds the maximum safe size for browser cookies, preventing silent authentication failures

#### Security
- **Hardcoded JWT Secret (Critical)**: The default JWT signing secret was committed to the public repository, allowing anyone to forge valid tokens for any user including admin — a full authentication bypass. `JwtTokenUtil` now detects the default secret at startup and forces operators to provide their own via the `APP_JWT_SECRET` environment variable
- **Security Filter Chain Separation**: Split the single `SecurityFilterChain` into two dedicated chains — a stateless chain for `/api/**` (JWT-based) and a stateful chain for web/OAuth2 endpoints — enabling independent session and authentication policies per surface

#### Features & Improvements
- **OpenTelemetry Tracing**: Added OpenTelemetry tracing dependencies to enable distributed tracing in production deployments
- **Validation Exception Handling**: `GlobalExceptionHandler` now handles `ValidationException` explicitly, returning structured error responses instead of generic 500s
- **Reduced Log Verbosity**: Lowered Tomcat request logging level to reduce noise in production logs
- **Dependency Upgrades**: Updated project dependencies to latest stable versions

### 🎨 Frontend (wisemapping-frontend)

#### Bug Fixes
- **Consent Dialog**: Fixed consent dialog failing to render correctly on initial page load

#### Features
- **Consent Management**: Replaced direct AdSense script injection with Google Funding Choices CMP integration for GDPR-compliant consent handling; subsequently refactored to a delayed AdSense loader to improve initial page load performance
- **Privacy Link**: Added privacy policy link to the application footer

#### Dependencies & Tooling
- **Vite 8**: Upgraded Vite 7 → 8 and `@vitejs/plugin-react` 5 → 6; migrated from `vite-plugin-html` to the native Vite 8 HTML transform API; replaced `vite-tsconfig-paths` with Vite's built-in `resolve.tsconfigPaths`
- **Storybook 10.3**: Updated Storybook ecosystem from 10.2 to 10.3; fixed build ordering by aliasing workspace packages directly to source
- **React & ESLint**: Updated React ecosystem packages; aligned ESLint React plugin to version 19.0.0
- **MUI**: Updated Material UI and styling dependencies including `@mui/lab`
- **Test Infrastructure**: Updated Cypress and test dependencies; fixed `afterEach` spy assertions that were crashing when spies were uninitialized
- **Linting & Formatting**: Updated ESLint, Prettier, and related tooling dependencies
- **Workspace Packages**: Migrated internal workspace cross-references to wildcard versions for easier monorepo maintenance

---

## September, 2025

### 🚀 Major Features & Enhancements

#### Theme System Overhaul
- **Dark Mode Support**: Added comprehensive dark mode support throughout the application
- **Theme Variants**: Implemented theme variant system with mandatory variant parameters for consistent theme resolution
- **New Themes**: Added Ocean theme variant with improved color schemes
- **Theme Styling**: Moved theme styles to JSON format for better maintainability and consistency
- **Font Color "None" Option**: Fixed font color "none" option to work correctly in both light and dark modes

#### Social Media Integration
- **Facebook Support**: Enabled Facebook social media integration for sharing and collaboration features

#### UI/UX Improvements
- **Look and Feel**: Significant improvements to overall application appearance and user experience
- **Relationship Styling**: Added enhanced styling options for mindmap relationships
- **Image Support**: Completed emoji-based image support implementation
- **Background Colors**: Added support for custom background colors in themes

#### Docker Distribution & Deployment
- **Full Stack Docker Image**: Added comprehensive Docker distribution with both frontend and backend in a single container
- **Multi-stage Build**: Implemented optimized multi-stage Dockerfile for efficient image building
- **All-in-One Deployment**: Created complete application deployment solution with Nginx reverse proxy
- **Container Orchestration**: Added Supervisor configuration for process management within containers
- **Production Ready**: Full stack Docker image ready for production deployment with persistent data support

### 🔧 Technical Improvements

#### Code Modernization
- **jQuery Removal**: Completed migration away from jQuery dependency
- **JavaScript Migration**: Completed .js to TypeScript migration across the codebase
- **Type Safety**: Enhanced TypeScript type safety for theme-related operations
- **Code Cleanup**: Removed unused imports, variables, and deprecated logic

#### Testing & Quality Assurance
- **Test Coverage**: Added multiple new test cases for improved code coverage
- **Linting**: Fixed various linting issues across the codebase
- **Compilation**: Resolved TypeScript compilation errors
- **Test Fixes**: Fixed and re-enabled previously failing tests

### 🐛 Bug Fixes

#### Rendering & Display
- **Render Bugs**: Fixed several rendering issues that affected mindmap display
- **Theme Selector**: Fixed theme selector background display issues
- **Image Rendering**: Fixed image rendering and event handling issues
- **Color Resolution**: Fixed color resolution issues in various theme modes

#### Analytics & Tracking
- **Analytics Events**: Improved analytics event tracking implementation
- **Topic Tracking**: Removed tracking for selected topic to improve performance and privacy

### 📦 Dependencies & Version Updates

#### Version Management
- **Version 6**: Updated application to version 6.0
- **Copyright**: Updated copyright information across the project
- **Package Updates**: Various package.json updates for consistency

### 🔄 Refactoring & Architecture

#### Theme Architecture
- **ThemeFactory**: Updated ThemeFactory.create() and createById() to require variant parameters
- **Theme Implementations**: Refactored all theme implementations (PrismTheme, EnhancedPrismTheme, DarkPrismTheme, ClassicTheme, RobotTheme)
- **Topic Styling**: Updated Topic class methods for better theme integration
- **Component Updates**: Updated all components to properly handle theme variants

#### Component Improvements
- **StandaloneActionDispatcher**: Enhanced color change methods to use Designer's current variant
- **NodePropertyBuilder**: Updated to pass variant when getting color values
- **MindplotWebComponent**: Improved theme resolution and variant handling

#### Infrastructure & Deployment
- **Docker Distribution**: Created comprehensive Docker distribution system with multiple deployment options
- **CI/CD Integration**: Updated GitHub Actions workflows for automated Docker image building and publishing
- **Multi-stage Builds**: Implemented efficient multi-stage Docker builds for both API-only and full-stack deployments
- **Container Orchestration**: Added Supervisor configuration for managing multiple processes in containers
- **Nginx Configuration**: Optimized Nginx reverse proxy setup for production deployments

### 📁 Files Modified

#### Core Theme Files
- `packages/mindplot/src/components/theme/Theme.ts`
- `packages/mindplot/src/components/theme/ThemeFactory.ts`
- `packages/mindplot/src/components/theme/DefaultTheme.ts`
- `packages/mindplot/src/components/theme/PrismTheme.ts`
- `packages/mindplot/src/components/theme/EnhancedPrismTheme.ts`
- `packages/mindplot/src/components/theme/DarkPrismTheme.ts`
- `packages/mindplot/src/components/theme/ClassicTheme.ts`
- `packages/mindplot/src/components/theme/RobotTheme.ts`

#### Component Updates
- `packages/mindplot/src/components/Topic.ts`
- `packages/mindplot/src/components/StandaloneActionDispatcher.ts`
- `packages/mindplot/src/components/DesignerActionRunner.ts`
- `packages/mindplot/src/components/MainTopic.ts`
- `packages/mindplot/src/components/ImageEmoji.ts`
- `packages/mindplot/src/components/ImageEmojiFeature.ts`
- `packages/mindplot/src/components/ConnectionLine.ts`

#### Editor Components
- `packages/editor/src/classes/model/node-property-builder/index.ts`
- Multiple editor component files updated for theme consistency

#### Docker Distribution Files
- `distribution/README.md` - Comprehensive Docker distribution documentation
- `distribution/app/Dockerfile` - Full stack multi-stage Docker build configuration
- `distribution/app/nginx.conf` - Production-ready Nginx reverse proxy configuration
- `distribution/app/supervisord.conf` - Container process management configuration
- `distribution/app/.dockerignore` - Docker build optimization
- `.github/workflows/docker-app-publish.yml` - Automated Docker image publishing workflow
- `.github/workflows/docker-api-publish.yml` - API-only Docker image publishing workflow

### 🎯 Impact Summary

This week's changes represent a major milestone in the Wisemapping Frontend project:

1. **Enhanced User Experience**: Dark mode support and improved theming provide users with better visual customization options
2. **Better Performance**: Removal of jQuery and code cleanup improve application performance
3. **Improved Maintainability**: JSON-based theme configuration and better type safety make the codebase more maintainable
4. **Social Integration**: Facebook support expands collaboration capabilities
5. **Quality Assurance**: Comprehensive testing improvements ensure more reliable functionality
6. **Production Deployment**: Docker distribution system enables easy deployment and scaling in production environments

### 🔮 Future Considerations

The theme system overhaul and dark mode implementation provide a solid foundation for:
- Additional theme variants and customization options
- Enhanced accessibility features
- Improved mobile responsiveness
- Advanced collaboration features

---

*This changelog is automatically generated based on git commit history and may be updated as new changes are merged.*
