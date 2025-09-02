FROM eclipse-temurin:24-jre-alpine
LABEL maintainer="Paulo Gustavo Veiga <pveiga@wisemapping.com>"

# Build argument to optionally download New Relic
ARG ENABLE_NEWRELIC=false

# Create non-root user for security
RUN addgroup -g 1001 -S wisemapping && \
    adduser -S -u 1001 -G wisemapping wisemapping

# Conditionally download New Relic agent
RUN if [ "$ENABLE_NEWRELIC" = "true" ]; then \
        apk add --no-cache curl && \
        curl -o /app/newrelic.jar https://download.newrelic.com/newrelic/java-agent/newrelic-agent/current/newrelic.jar && \
        chown wisemapping:wisemapping /app/newrelic.jar && \
        apk del curl; \
    fi

VOLUME /tmp
COPY --chown=wisemapping:wisemapping env-config.sh /app/config-gen.sh
COPY --chown=wisemapping:wisemapping target/wisemapping-api.jar /app/wisemapping-api.jar
RUN chmod +x /app/config-gen.sh

WORKDIR /app
USER wisemapping

ENTRYPOINT ["sh", "-c", "/app/config-gen.sh && java ${JAVA_OPTS} ${NEW_RELIC_OPTS} -jar wisemapping-api.jar"]