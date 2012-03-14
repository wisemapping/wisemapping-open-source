# WiseMapping: a Web based mindmapping application

## Project Information

The goal of this project is to provide a high quality product that can be deployed by educational and academic institutions, private and public companies and anyone who needs to have a mindmapping application. WiseMapping is based on the same code source supporting WiseMapping.com. More info: www.wisemapping.org

## Compiling and Running

### Prerequisites

The following products must be installed:
    * Java Development Kit 6 or higher (http://java.sun.com/javase/downloads/index.jsp)
    * Maven 2.2.1 or higher (http://maven.apache.org/)

### Compiling

WiseMapping uses Maven as packaging and project management. The project is composed of 4 maven sub-modules:
    * core-js: Utilities JavaScript libraries
    * web2d: JavaScript 2D SVG abstraction library used by the mind map editor
    * mindplot: JavaScript mind map designer core
    * wise-webapp: J2EE web application 

Full compilation of the project can be done executing within <project-dir>:

`mvn install`

Once this command is execute, the file <project-dir>/wise-webapp/target/wisemapping.war will be generated.

### Testing

The previously generated war can be deployed locally executing within the directory <project-dir>/wise-webapp the following command:

`mvn jetty:run-war`

This will start the application on the URL: http://localhost:8080/wise-webapp/. Additionally, a file based database is automatically populated with a test user.

User: test@wisemapping.org


## Running the JS only version

Start by creating the .zip file: 

`mvn assembly:assembly -Dmaven.test.skip=true`

## Author

   * Pablo Luna
   * Paulo Veiga
   * Ignacio Manzano
   * Nicolas Damonte

## License

The source code is Licensed under the WiseMapping Open License, Version 1.0 (the “License”);

You may obtain a copy of the License at: http://www.wisemapping.org/license
