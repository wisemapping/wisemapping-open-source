# Compiling and Running

## Prerequisites

The following products must be installed:

    * Java Development Kit 7 or higher ([http://www.oracle.com/technetwork/java/javase/downloads/index.html])
    * Maven 3.x or higher ([http://maven.apache.org/])

## Compiling

WiseMapping uses Maven as packaging and project management. It's composed of 5 maven sub-modules:

    * core-js: Utilities JavaScript classes
    * web2d: JavaScript 2D SVG abstraction library used by the mind map editor
    * mindplot: JavaScript mindmap designer core
    * wise-editor: Mindmap Editor standalone distribution
    * wise-webapp: J2EE web application 

The full compilation of the project can be performed executing within <project-dir>:

`mvn package`

Once this command is execute, the file <project-dir>/wise-webapp/target/wisemapping*.war will be generated.

## Testing
The previously generated war can be deployed locally executing within the directory <project-dir>/wise-webapp the following command:

`cd wise-webapp;mvn jetty:run-war`

This will start the application on the URL: [http://localhost:8080/wise-webapp/]. Additionally, a file based database is automatically populated with a test user.

User: test@wisemapping.org
Password: test

Enjoy :)