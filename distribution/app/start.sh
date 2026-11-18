#!/bin/sh
# PID 1 is tini; this script launches nginx in the background and execs the
# Spring Boot JAR as a non-root user in the foreground. When java exits, tini
# tears down the container (which also reaps nginx via SIGTERM).
set -e

nginx -g 'daemon off;' &

# -XX:-UseBASE64Intrinsics mitigates CVE-2026-114938 ("Java4Shell") on JDKs older
# than 27.0.1.1. Kept outside JAVA_OPTS so overriding JAVA_OPTS cannot drop it.
exec su-exec wisemapping:wisemapping sh -c "java -XX:-UseBASE64Intrinsics ${JAVA_OPTS} \
  -Dspring.config.additional-location=optional:file:/app/config/ \
  -jar /app/wisemapping-api.jar"
