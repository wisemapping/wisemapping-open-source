# Dockerizing WiseMapping: Dockerfile for building WiseMapping images
# Based on ubuntu:latest, installs WiseMapping (http://ww.wisemapping.org)

# Based info setup ...
FROM tomcat:9.0-jdk17-openjdk
LABEL maintainer="Paulo Gustavo Veiga <pveiga@wisemapping.com>"

#Build variables ...
ARG WEBAPP_TARGET_DIR="/usr/local/tomcat/webapps/ROOT"
ENV database.base.url="/var/wisemapping"

# Copy wisemapping distribution ...
COPY wisemapping.war /tmp
RUN mkdir ${WEBAPP_TARGET_DIR}
RUN cd ${WEBAPP_TARGET_DIR} && jar -xvf /tmp/wisemapping.war
RUN rm /tmp/wisemapping.war

# Change logger to
RUN cp ${WEBAPP_TARGET_DIR}/WEB-INF/classes/log4j-stdout.properties ${WEBAPP_TARGET_DIR}/WEB-INF/classes/log4j.properties

# Copy default HSQL DB for testing ...
RUN mkdir -p /var/wisemapping/db
COPY db/ /var/wisemapping/db
