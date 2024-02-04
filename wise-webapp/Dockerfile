FROM amazoncorretto:17.0.10

VOLUME /tmp
COPY target/wisemapping-api.jar app.jar
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar /app.jar ${0} ${@}"]