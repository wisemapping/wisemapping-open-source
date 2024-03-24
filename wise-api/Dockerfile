FROM amazoncorretto:21.0.2
LABEL maintainer="Paulo Gustavo Veiga <pveiga@wisemapping.com>"

VOLUME /tmp
COPY target/wisemapping-api.jar wisemapping-api.jar

ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar /wisemapping-api.jar ${0} ${@}"]