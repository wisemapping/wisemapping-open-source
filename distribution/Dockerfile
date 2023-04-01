# Dockerizing WiseMapping: Dockerfile for building WiseMapping images
# Based on ubuntu:latest, installs WiseMapping (http://ww.wisemapping.org)

# Based info setup ...
FROM tomcat:9.0.62-jdk17
LABEL maintainer="Paulo Gustavo Veiga <pveiga@wisemapping.com>"

# Build variables ...
ARG WEBAPP_TARGET_DIR="/usr/local/tomcat/webapps/ROOT"
ARG DB_BASE_DIR="/var/lib/wisemapping"

# Default ENV configurations ...
ENV JAVA_OPTS="-XX:+PrintFlagsFinal -XX:InitialRAMPercentage=30 -XX:MaxRAMPercentage=80 -Dfile.encoding=UTF-8"
ENV database.base.url=${DB_BASE_DIR}

# Copy wisemapping distribution ...
COPY wisemapping.war /tmp
RUN mkdir ${WEBAPP_TARGET_DIR}
RUN cd ${WEBAPP_TARGET_DIR} && jar -xvf /tmp/wisemapping.war
RUN rm /tmp/wisemapping.war

# Change logger to
RUN cp ${WEBAPP_TARGET_DIR}/WEB-INF/classes/log4j-stdout.properties ${WEBAPP_TARGET_DIR}/WEB-INF/classes/log4j.properties

# Add support for proxy
RUN sed -i 's|\
  </Host>|\
    <Valve className="org.apache.catalina.valves.RemoteIpValve" \
      remoteIpHeader="X-Forwarded-For" \
      protocolHeader="X-Forwarded-Proto"/>\
  </Host>|' \
  /usr/local/tomcat/conf/server.xml

RUN sed -i 's|<Context>|<Context>\
    <Loader jakartaConverter="TOMCAT" />|' \
  /usr/local/tomcat/conf/context.xml
# Copy default HSQL DB for testing ...
RUN mkdir -p ${DB_BASE_DIR}/db
COPY db/ ${DB_BASE_DIR}/db
