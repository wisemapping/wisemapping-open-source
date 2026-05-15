################################################################################
# Multi-stage Dockerfile for WiseMapping (Frontend + Backend)
# This Dockerfile builds both the backend API and frontend UI in a single image
################################################################################

################################################################################
# Stage 1: Build Backend (Spring Boot API)
################################################################################
FROM eclipse-temurin:24-jdk-alpine AS backend-builder
LABEL maintainer="Paulo Gustavo Veiga <pveiga@wisemapping.com>"

WORKDIR /build

# Install Maven
RUN apk add --no-cache maven

# Copy backend source code
COPY wise-api/pom.xml ./wise-api/
COPY wise-api/src ./wise-api/src/

# Build the backend JAR
WORKDIR /build/wise-api
RUN mvn clean package -DskipTests \
    -Dgit.branch=unknown \
    -Dgit.commit.id.abbrev=unknown \
    -Dgit.commit.id.full=unknown \
    -Dgit.commit.time=unknown

################################################################################
# Stage 2: Build Frontend (React/TypeScript)
################################################################################
FROM node:24-alpine AS frontend-builder

WORKDIR /build

ARG FRONTEND_BRANCH="develop"

# Install git
RUN apk add --no-cache git

# Add cache-busting argument to force fresh git clone
ARG CACHEBUST=1

# Clone the frontend repository (always fetch latest from branch)
RUN git clone --depth 1 --branch ${FRONTEND_BRANCH} https://github.com/wisemapping/wisemapping-frontend.git

# Patch the production config to use relative URL
WORKDIR /build/wisemapping-frontend/packages/webapp
RUN sed -i 's|"url": "http://localhost:3000/api/restful/app/config"|"url": "/api/restful/app/config"|' config.prod.json

# Build the frontend (will use patched config.prod.json)
WORKDIR /build/wisemapping-frontend
ENV COREPACK_ENABLE_STRICT=0
RUN corepack enable && corepack prepare yarn@4.13.0 --activate
RUN yarn install
ENV NODE_OPTIONS="--max-old-space-size=4096"
RUN APP_CONFIG_TYPE=file:prod yarn build --concurrency 1

# Ensure proper base tag for nginx sub_filter replacement:
# 1. Remove any base tag webpack created (with empty, relative, or absolute href)
# 2. Add our placeholder <base> tag for nginx to replace with absolute URL at runtime
RUN sed -i 's|<base[^>]*>||g' /build/wisemapping-frontend/packages/webapp/dist/index.html && \
    sed -i 's|<head>|<head>\n  <base>|' /build/wisemapping-frontend/packages/webapp/dist/index.html  

################################################################################
# Stage 3: Final Runtime Image (Backend + Frontend + Nginx)
################################################################################
FROM eclipse-temurin:24-jre-alpine
LABEL maintainer="Paulo Gustavo Veiga <pveiga@wisemapping.com>"

# Install Nginx, tini (PID 1 init) and su-exec (privilege drop).
# Supervisor is intentionally NOT installed: its Python dependency on
# Alpine breaks at runtime due to a libexpat/python skew (see issue #70).
RUN apk add --no-cache nginx tini su-exec

# Create non-root user for security
RUN addgroup -g 1001 -S wisemapping && \
    adduser -S -u 1001 -G wisemapping wisemapping

# Create necessary directories
RUN mkdir -p /app \
    /var/log/nginx \
    /run/nginx \
    /usr/share/nginx/html \
    /var/lib/nginx/tmp/client_body \
    /var/lib/nginx/tmp/proxy && \
    chown -R wisemapping:wisemapping /app \
    /var/log/nginx \
    /run/nginx \
    /usr/share/nginx/html && \
    chown -R 100:101 /var/lib/nginx

# Copy backend JAR
COPY --from=backend-builder --chown=wisemapping:wisemapping /build/wise-api/target/wisemapping-api.jar /app/wisemapping-api.jar

# Create config directory for optional app.yml
RUN mkdir -p /app/config && chown wisemapping:wisemapping /app/config

# Copy frontend build
COPY --from=frontend-builder --chown=wisemapping:wisemapping /build/wisemapping-frontend/packages/webapp/dist /usr/share/nginx/html/

# Copy Nginx configuration
COPY distribution/app/nginx.conf /etc/nginx/http.d/default.conf

# Copy startup script (launches nginx + Spring Boot under tini)
COPY distribution/app/start.sh /app/start.sh
RUN chmod +x /app/start.sh

# Expose port 80 only (Nginx handles routing to backend)
EXPOSE 80

WORKDIR /app

ENTRYPOINT ["/sbin/tini", "--"]
CMD ["/app/start.sh"]
