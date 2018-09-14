# Project Information

The goal of this project is to provide a high quality product that can be deployed by educational and academic institutions, private and public companies and anyone who needs to have a mindmapping application. WiseMapping is based on the same code source supporting WiseMapping.com. More info: www.wisemapping.org

## Compiling and Running

### Prerequisites

The following products must be installed:

   * Java Development Kit 7 or higher ([http://www.oracle.com/technetwork/java/javase/downloads/index.html])
   * Maven 3.x or higher ([http://maven.apache.org/])

### Compiling

WiseMapping uses Maven as packaging and project management. It's composed of 5 maven sub-modules:

    * core-js: Utilities JavaScript classes
    * web2d: JavaScript 2D SVG abstraction library used by the mind map editor
    * mindplot: JavaScript mindmap designer core
    * wise-editor: Mindmap Editor standalone distribution
    * wise-webapp: J2EE web application 

The full compilation of the project can be performed executing within <project-dir>:

`mvn package`

Once this command is execute, the file <project-dir>/wise-webapp/target/wisemapping*.war will be generated.

### Testing
The previously generated war can be deployed locally executing within the directory <project-dir>/wise-webapp the following command:

`cd wise-webapp;mvn jetty:run-war`

This will start the application on the URL: [http://localhost:8080/wise-webapp/]. Additionally, a file based database is automatically populated with a test user.

User: test@wisemapping.org
Password: test

## Running the JS only version

Start by creating the .zip file:

`mvn assembly:assembly -Dmaven.test.skip=true`

To test the javascript frontend you then do:

    ruby -rwebrick -e 'WEBrick::HTTPServer.new(:Port=>8000,:DocumentRoot=>".").start'

Now open a browser using the URL http://localhost:8000/wise-editor/src/main/webapp/

## Members

### Founders

   * Pablo Luna <pveiga@wisemapping.com>
   * Paulo Veiga <pablo@wisemapping.com>

### Individual Controbutors

   * Ezequiel Bergamaschi <ezequielbergamaschi@gmail.com>
   * Claudio Barril <claudiobarril@gmail.com>

### Past Individual Contributors

   * Ignacio Manzano
   * Nicolas Damonte
   
## License

The source code is Licensed under the WiseMapping Open License, Version 1.0 (the “License”);
You may obtain a copy of the License at: [https://wisemapping.atlassian.net/wiki/display/WS/License]
