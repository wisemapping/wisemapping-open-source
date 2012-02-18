# Compiling and Running

## Prerequisites

The following products must be installed:

    * Java Development Kit 6 or higher (http://java.sun.com/javase/downloads/index.jsp)
    * Maven 3.x or higher (http://maven.apache.org/)

## Compiling

WiseMapping uses Maven as packaging and project management. The project is composed of 4 maven sub-modules:

    * core-js: Utilities JavaScript libraries
    * web2d: JavaScript 2D VML/SVG abstraction library used by the mind map editor
    * mindplot: JavaScript mind map designer core
    * wise-editor: Mindmap Editor standalone distribution
    * wise-webapp: J2EE web application 

Full compilation of the project can be done executing within <project-dir>:

`mvn package`

Once this command is execute, the file <project-dir>/wise-webapp/target/wisemapping*.war will be generated.

## Testing
The previously generated war can be deployed locally executing within the directory <project-dir>/wise-webapp the following command:

`mvn jetty:run-war`

This will start the application on the URL: http://localhost:8080/wise-webapp/. Additionally, a file based database is automatically populated with a test user.

User: test@wisemapping.org
Password: test