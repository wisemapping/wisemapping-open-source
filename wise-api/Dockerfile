FROM eclipse-temurin:21-jre-alpine
LABEL maintainer="Paulo Gustavo Veiga <pveiga@wisemapping.com>"

# Create non-root user for security
RUN addgroup -g 1001 -S wisemapping && \
    adduser -S -u 1001 -G wisemapping wisemapping

VOLUME /tmp
COPY --chown=wisemapping:wisemapping env-config.sh /app/config-gen.sh
COPY --chown=wisemapping:wisemapping target/wisemapping-api.jar /app/wisemapping-api.jar
RUN chmod +x /app/config-gen.sh

WORKDIR /app
USER wisemapping

ENTRYPOINT ["sh", "-c", "/app/config-gen.sh && java ${JAVA_OPTS} -jar wisemapping-api.jar"]