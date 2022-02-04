# Dockerizing WiseMapping: Dockerfile for building WiseMapping images
# Based on ubuntu:latest, installs WiseMapping (http://ww.wisemapping.org)

# Based info setup ...
FROM tomcat:9.0-jdk17-openjdk
LABEL maintainer="Paulo Gustavo Veiga <pveiga@wisemapping.com>"

# Build variables ...
ARG WEBAPP_TARGET_DIR="/usr/local/tomcat/webapps/ROOT"
ARG DB_BASE_DIR="/var/lib/wisemapping"

# Defautl ENV configurations ...
ENV JAVA_OPTS="-XX:+PrintFlagsFinal -XX:InitialRAMPercentage=30 -XX:MaxRAMPercentage=80 -Dnewrelic.config.log_file_name=STDOUT"ENV database.base.url=${DB_BASE_DIR}

# Copy wisemapping distribution ...
COPY wisemapping.war /tmp
RUN mkdir ${WEBAPP_TARGET_DIR}
RUN cd ${WEBAPP_TARGET_DIR} && jar -xvf /tmp/wisemapping.war
RUN rm /tmp/wisemapping.war

# Change logger to
RUN cp ${WEBAPP_TARGET_DIR}/WEB-INF/classes/log4j-stdout.properties ${WEBAPP_TARGET_DIR}/WEB-INF/classes/log4j.properties

# Copy default HSQL DB for testing ...
RUN mkdir -p ${DB_BASE_DIR}/db
COPY db/ ${DB_BASE_DIR}/db
