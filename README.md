# Overview

Wise Mapping is the web mind mapping open source tool that leverages the power of Mind Maps mixing open standards technologies such as SVG and React.
WiseMapping is based on the same code product supporting [http://www.wisemapping.com]. 

## Compiling and Running

### Prerequisites

The following products must be installed:

    * OpenJDK 11 or higher
    * Maven 3.x or higher ([http://maven.apache.org/])
    * npm 6 or higher ([https://www.npmjs.com/package/npm?activeTab=versions])

### Compiling

WiseMapping uses Maven as packaging and project management. It's composed of 5 maven sub-modules:

    * wise-ui:  React font-end fetcher
    * wise-webapp: J2EE web application 

The full compilation of the project can be performed executing within <project-dir>:

`mvn clean install`

Once this command is executed, the file <project-dir>/wise-webapp/target/wisemapping*.war will be generated.

### Local Development
The previously generated war can be deployed locally executing within the directory <project-dir>/wise-webapp the following command:

`cd wise-webapp;mvn jetty:run-war`

This will start the application on the URL: [http://localhost:8080/] using file based database.

User: test@wisemapping.org
Password: test

### Local Development + UI Integration

In order to reduce the life-cycle to develop UI backend testing, you can do the following hack:

* Clone [wisemapping-open-source](https://bitbucket.org/wisemapping/wisemapping-open-source/) and [wisemapping-frontend](https://bitbucket.org/wisemapping/wisemapping-frontend/) at the same top level directory
* Compile `wisemapping-frontend`. Details for compilation can be found in the `wisemapping-frontend` readme.
* Compile `wisemapping-open-source`

A quick and dirty solution to share changes in the UI is to manually compile the dist. This will make the loader file available without the need to publish:

`yarn --cwd wisemapping-frontend build;cp -r wisemapping-frontend/packages/mindplot/dist/* wisemapping-open-source/wise-ui/target/wisemapping-mindplot/package/dist;cp -r wisemapping-frontend/packages/webapp/dist/* wisemapping-open-source/wise-ui/target/wisemapping-webapp/package/dist`


### Compiling and running with docker-compose

Check out the [docker section](./docker/README.)

### Test reports

Individual test result reports can be found in wisemapping-open-source/wise-webapp/target/failsafe-reports/index.html
Test coverage report of unit and integration test can be found in wisemapping-open-source/wise-webapp/target/site/jacoco and wisemapping-open-source/wise-webapp/target/site/jacoco-it folders. Coverage report is generated in the verify phase of [lifecicle](https://maven.apache.org/guides/introduction/introduction-to-the-lifecycle.html#introduction-to-the-build-lifecyclea) using [jacoco](https://www.jacoco.org/jacoco/trunk/doc/maven.html)

## Members

### Founders

   * Paulo Veiga <pveiga@wisemapping.com>
   * Pablo Luna <pablo@wisemapping.com>

### Past Individual Contributors

   * Ignacio Manzano  
   * Ezequiel Bergamaschi <ezequielbergamaschi@gmail.com>
   
## License

The source code is Licensed under the WiseMapping Open License, Version 1.0 (the “License”);
You may obtain a copy of the License at: [https://wisemapping.atlassian.net/wiki/display/WS/License]

