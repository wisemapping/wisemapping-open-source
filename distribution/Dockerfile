# Dockerizing WiseMapping: Dockerfile for building WiseMapping images
# Based on ubuntu:latest, installs WiseMapping (http://ww.wisemapping.org)

FROM       ubuntu:latest
MAINTAINER Paulo Gustavo Veiga <pveiga@wisemapping.com>

ENV DEBIAN_FRONTEND noninteractive
ENV MYSQL_ROOT_PASSWORD password
ENV WISE_VERSION 4.0.1

# Install utilities
RUN apt-get install -y zip

# Prepare distribution
COPY target/wisemapping-v${WISE_VERSION}.zip .
RUN unzip wisemapping-v${WISE_VERSION}.zip

# Install MySQL
RUN echo mysql-server mysql-server/root_password password ${MYSQL_ROOT_PASSWORD} | debconf-set-selections;\
    echo mysql-server mysql-server/root_password_again password ${MYSQL_ROOT_PASSWORD} | debconf-set-selections;\
    apt-get install -y mysql-server

RUN service mysql start && \
    mysql -uroot -p${MYSQL_ROOT_PASSWORD} < /wisemapping-v${WISE_VERSION}/config/database/mysql/create-database.sql && \
    mysql -uwisemapping -Dwisemapping -ppassword < /wisemapping-v${WISE_VERSION}/config/database/mysql/create-schemas.sql && \
    mysql -uwisemapping -Dwisemapping -ppassword < /wisemapping-v${WISE_VERSION}/config/database/mysql/apopulate-schemas.sql

# Install Java 8
RUN apt-get install -y software-properties-common && \
    add-apt-repository ppa:webupd8team/java && \
    apt-get update

RUN echo oracle-java8-installer shared/accepted-oracle-license-v1-1 select true | debconf-set-selections;\
    echo oracle-java8-installer shared/accepted-oracle-license-v1-1 seen true | debconf-set-selections;\
    sudo apt-get install -y oracle-java8-installer

# Clean up
RUN apt-get clean
RUN rm wisemapping-v${WISE_VERSION}.zip


EXPOSE 8080

CMD "echo" "Starting WiseMapping ${WISE_VERSION}"
CMD “cd wisemapping-v${WISE_VERSION};./start.sh”
