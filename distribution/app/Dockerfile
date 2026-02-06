################################################################################
# Multi-stage Dockerfile for WiseMapping (Frontend + Backend)
# This Dockerfile builds both the backend API and frontend UI in a single image
################################################################################

################################################################################
# Stage 1: Build Backend (Spring Boot API)
################################################################################
FROM --platform=$BUILDPLATFORM eclipse-temurin:24-jdk-alpine AS backend-builder
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
FROM --platform=$BUILDPLATFORM node:22-alpine AS frontend-builder

WORKDIR /build

ARG FRONTEND_BRANCH="develop"

# Install git
RUN apk add --no-cache git bash

# Add cache-busting argument to force fresh git clone
ARG CACHEBUST=1

# Clone the frontend repository (always fetch latest from branch)
RUN git clone --depth 1 --branch ${FRONTEND_BRANCH} https://github.com/wisemapping/wisemapping-frontend.git

# Patch the production config to use relative URL
WORKDIR /build/wisemapping-frontend/packages/webapp
RUN sed -i 's|"url": "http://localhost:3000/api/restful/app/config"|"url": "/api/restful/app/config"|' config.prod.json

# Build the frontend (will use patched config.prod.json)
WORKDIR /build/wisemapping-frontend
RUN corepack enable && corepack prepare yarn@1.22.22 --activate
RUN yarn install --network-timeout 100000
ENV NODE_OPTIONS="--max-old-space-size=6144"
RUN APP_CONFIG_TYPE=file:prod GENERATE_SOURCEMAP=false yarn build

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

# Install Nginx and required packages
RUN apk add --no-cache nginx supervisor

# Create non-root user for security
RUN addgroup -g 1001 -S wisemapping && \
    adduser -S -u 1001 -G wisemapping wisemapping

# Create necessary directories
RUN mkdir -p /app \
    /var/log/nginx \
    /var/log/supervisor \
    /run/nginx \
    /usr/share/nginx/html && \
    chown -R wisemapping:wisemapping /app \
    /var/log/nginx \
    /var/log/supervisor \
    /run/nginx \
    /usr/share/nginx/html \
    /var/lib/nginx

# Copy backend JAR
COPY --from=backend-builder --chown=wisemapping:wisemapping /build/wise-api/target/wisemapping-api.jar /app/wisemapping-api.jar

# Create config directory for optional app.yml
RUN mkdir -p /app/config && chown wisemapping:wisemapping /app/config

# Copy frontend build
COPY --from=frontend-builder --chown=wisemapping:wisemapping /build/wisemapping-frontend/packages/webapp/dist /usr/share/nginx/html/

# Copy Nginx configuration
COPY distribution/app/nginx.conf /etc/nginx/http.d/default.conf

# Copy Supervisor configuration
COPY distribution/app/supervisord.conf /etc/supervisord.conf

# Expose port 80 only (Nginx handles routing to backend)
EXPOSE 80

WORKDIR /app

# Start Supervisor to manage both Nginx and Spring Boot
CMD ["/usr/bin/supervisord", "-c", "/etc/supervisord.conf"]
