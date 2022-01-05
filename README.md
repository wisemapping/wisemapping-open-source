# Project Information

The goal of this project is to provide a high quality product that can be deployed by educational and academic institutions, private and public companies and anyone who needs to have a mindmapping application. WiseMapping is based on the same code source supporting WiseMapping.com. More info: www.wisemapping.org

## Compiling and Running

### Prerequisites

The following products must be installed:

    * Java 11 or higher
    * Maven 3.x or higher ([http://maven.apache.org/])

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

### Local Development - UI Integration

In order to reduce the life-cycle to develop UI backend testing, you can do the following hack:

* Clone [https://bitbucket.org/wisemapping/wisemapping-open-source/] and [https://bitbucket.org/wisemapping/wisemapping-frontend/] at the same top level directory
* Compile `WiseMapping Frontend`
* Compile `WiseMapping Open Source`
* Follow the Local Testing steps.

### Compiling and running with docker-compose

Check out the [docker section](./docker/README.
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

   * Paulo Veiga <pablo@wisemapping.com>
   * Pablo Luna <pveiga@wisemapping.com>

### Individual Contributors

   * Ezequiel Bergamaschi <ezequielbergamaschi@gmail.com>

### Past Individual Contributors

   * Ignacio Manzano
   
## License

The source code is Licensed under the WiseMapping Open License, Version 1.0 (the “License”);
You may obtain a copy of the License at: [https://wisemapping.atlassian.net/wiki/display/WS/License]

