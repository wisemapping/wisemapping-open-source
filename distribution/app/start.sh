#!/bin/sh
# PID 1 is tini; this script launches nginx in the background and execs the
# Spring Boot JAR as a non-root user in the foreground. When java exits, tini
# tears down the container (which also reaps nginx via SIGTERM).
set -e

nginx -g 'daemon off;' &

exec su-exec wisemapping:wisemapping sh -c "java ${JAVA_OPTS} \
  -Dspring.config.additional-location=optional:file:/app/config/ \
  -jar /app/wisemapping-api.jar"
