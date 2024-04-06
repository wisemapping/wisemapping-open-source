# Overview

WiseMapping is an open-source web-based mind mapping tool that harnesses the potential of Mind Maps by blending together open standards technologies like SVG and React. It is built upon the foundation of the code supporting http://www.wisemapping.com, ensuring reliability and continuity in its development.

# Compile and Development

The following section describes the steps to check out, compile, and start WiseMapping locally. If you are interested in deploying it, I recommend using the already published images https://hub.docker.com/r/wisemapping/wisemapping.

## Prerequisites

    * JDK 21 or higher
    * Maven v3.x or higher ([http://maven.apache.org/])
    * Yarn v1 or higher
    * Node v18 or higher

## Option 1: Quick Start with Docker Compose

The following command line will start WiseMapping locally using HSQLDB in memory for development purposes:

```
$ mvn -f wise-api/pom.xml package
$ docker compose up --build
```

Application will start at http://localhost/c/login. You can login using *test@wisemapping.org* and password *test*

## Option 2: Start Frontend and Backend API

### Compile and Start API

```
$ mvn -f wise-api/pom.xml package
$ cd wise-api
$ mvn spring-boot:run
```

### Compile and Start Frontend

You need to checkout https://github.com/wisemapping/wisemapping-frontend first. Then, follow the next steps:

```
$ export NODE_OPTIONS=--openssl-legacy-provider
$ export APP_CONFIG_TYPE="file:dev"

$ cd wisemapping-frontend
$ yarn install 
$ yarn build

$ cd packages/webapp; yarn start
```
Application will start at http://localhost:3000/c/login. You can login using *test@wisemapping.org* and password *test*

# Supportability Matrix

## Supported Databases

* MySQL v8 or higher
* PostgreSQL v15 or higher
* Hsqldb v2.7 or higher


# Configuration

WiseMapping backend is based on SpringBoot v3 and it's highly customizable. Additional documentation can be found [here](https://docs.spring.io/spring-boot/3.3/reference/features/external-config.html)

The perfered option is to extended by overwriting [application.yaml](https://github.com/wisemapping/wisemapping-open-source/blob/develop/wise-api/src/main/resources/application.yml)

```
$ java -jar target/wisemapping-api.jar --spring.config.additional-location=../../wise-conf/app.yml
```

For example, this [example](https://github.com/wisemapping/wisemapping-open-source/blob/develop/config/database/postgresql/app-postgresql.yaml) configure PostgreSQL as database.

# Members

## Founders

   * Paulo Veiga <pveiga@wisemapping.com>
   * Pablo Luna <pablo@wisemapping.com>

## Past Individual Contributors

   * Ignacio Manzano  
   * Ezequiel Bergamaschi <ezequielbergamaschi@gmail.com>
   
## License

The source code is Licensed under the WiseMapping Open License, Version 1.0 (the “License”);
You may obtain a copy of the License at: [https://github.com/wisemapping/wisemapping-open-source/blob/develop/LICENSE.md](https://github.com/wisemapping/wisemapping-open-source/blob/develop/LICENSE.md)

