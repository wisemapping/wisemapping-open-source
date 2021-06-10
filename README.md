# Project Information

The goal of this project is to provide a high quality product that can be deployed by educational and academic institutions, private and public companies and anyone who needs to have a mindmapping application. WiseMapping is based on the same code source supporting WiseMapping.com. More info: www.wisemapping.org

## Compiling and Running

### Prerequisites

The following products must be installed:

    * Java Development Kit 8 or higher ([http://www.oracle.com/technetwork/java/javase/downloads/index.html])
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

### Compiling and running with docker-compose

Check out the [docker section](./docker/README.md)

### Testing
The previously generated war can be deployed locally executing within the directory <project-dir>/wise-webapp the following command:

`cd wise-webapp;mvn jetty:run-war`

This will start the application on the URL: [http://localhost:8080/] using file based database..

User: test@wisemapping.org
Password: test

## Running the JS only version

Start by creating the .zip file:

`mvn assembly:assembly -Dmaven.test.skip=true`

To test the javascript frontend you then do:

    ruby -rwebrick -e 'WEBrick::HTTPServer.new(:Port=>8000,:DocumentRoot=>".").start'

Now open a browser using the URL http://localhost:8000/wise-editor/src/main/webapp/

## Troubleshooting

<details>
    <summary>
    <code>mvn package</code> fails with the error <code>java.lang.UnsatisfiedLinkError: Can't load library: /usr/lib/jvm/java-11-openjdk-amd64/lib/libawt_xawt.so</code> in Ubuntu
    </summary>

    Make sure you have the jdk installed: `sudo apt-get install openjdk-11-jdk`
</details>

<details>
    <summary><code>mvn package</code> does not generate the wisemapping.war file </summary>

    Run `mvn clean install -DskipTests`
</details>

## Maintenance



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

