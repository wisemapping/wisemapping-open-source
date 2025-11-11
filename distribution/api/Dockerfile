FROM eclipse-temurin:24-jre-alpine
LABEL maintainer="Paulo Gustavo Veiga <pveiga@wisemapping.com>"

# Build argument to optionally download New Relic
ARG ENABLE_NEWRELIC=false

# Create non-root user for security
RUN addgroup -g 1001 -S wisemapping && \
    adduser -S -u 1001 -G wisemapping wisemapping

VOLUME /tmp
COPY --chown=wisemapping:wisemapping distribution/api/env-config.sh /app/config-gen.sh
COPY --chown=wisemapping:wisemapping wise-api/target/wisemapping-api.jar /app/wisemapping-api.jar
RUN chmod +x /app/config-gen.sh

# Create app directory and conditionally download New Relic agent
RUN mkdir -p /app && \
    if [ "$ENABLE_NEWRELIC" = "true" ]; then \
        apk add --no-cache curl && \
        curl -o /app/newrelic.jar https://download.newrelic.com/newrelic/java-agent/newrelic-agent/current/newrelic.jar && \
        chown wisemapping:wisemapping /app/newrelic.jar && \
        apk del curl; \
    fi

WORKDIR /app
USER wisemapping

ENV JAVA_OPTS="-XX:InitialRAMPercentage=60 -XX:MaxRAMPercentage=70"

ENTRYPOINT ["sh", "-c", "/app/config-gen.sh && java ${JAVA_OPTS} ${NEW_RELIC_OPTS} -Dspring.config.additional-location=optional:file:/app/config/ -jar wisemapping-api.jar"]