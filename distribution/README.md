This is an approach to compiling and installing wisemapping using docker and docker-compose.

In this way you can run the app without installing specific java, maven, tomcat and mysql versions in the host machine just for this project.

# Prerequisites

Make sure you have [docker](https://docs.docker.com/engine/install/) and [docker-compose](https://docs.docker.com/compose/install/) installed. You might also want to [run docker without sudo](https://docs.docker.com/engine/install/linux-postinstall/) if you are on linux.

# Compile wisemapping using docker

This is not required to run the app using docker. You might skip this section if you already have the `war` file or if you want to compile it by yourself.

We create a volume so the downloaded packages can be reused across different containers (a new container is created each time we run the image). This is run only once.

```
docker volume create --name maven-repo-wisemapping
```

Then we can run the following command from the project root:

```
docker run -it --rm --name wisemapping-compile \
  -v maven-repo-wisemapping:/root/.m2 \
  -v "$(pwd)":/usr/src/mymaven -w /usr/src/mymaven maven:3-jdk-11 \
  mvn clean install -DskipTests
```

After that, you can find the result of the compilation in `wise-webapp/target/wisemapping.war`.

# Run using docker-compose

## Edit config

### app.properties

First of all, edit your `wise-webapp/src/main/webapp/WEB-INF/app.properties` to configure the app to use mysql.

- Uncomment the `MySQL 5.X configuration properties` section
- Change the `database.url` line to this: `database.url=jdbc:mysql://db/wisemapping?useUnicode=yes&characterEncoding=UTF-8` (the host is "db" instead of "localhost")
- Change the default username and password
- Comment the `HSQL Configuration properties` section

> Any time you make any config modification, you will have to re-compile the project, using the docker build shown before or any other method.

### docker-compose.yml

Review the `docker-compose.yml` file and edit it with your settings.
In the example provided the important bits are:

- `/opt/wisemapping-db`: this is where the mysql database files will be stored in your machine.
- Change the default password for the database to match your app.properties password. Please don't keep "password"!
- You might want to remove the `ports:` section in the db service if you don't want your db exposed to the outside.

```
ports:
  - 3306:3306
```

- You might want to change the port mapping for web (by default it will run in port 8082)

```
ports:
  - "8082:8080"
```

- Change `../wise-webapp/target/wisemapping.war` if you want to store the war file anywhere else in your machine. If you leave the default, the war file deployed will be overriden each time you build the app. You might not want that behavior.

## Running

Once the configs are ready, from this folder, run `docker-compose -p wise-webapp up -d` to run the web and the database containers as daemons. They will start automatically whith the machine.

You can check your docker running containers with `docker ps`.

To stop them you can run `docker-compose down` from this directory (doing this, they won't start automatically anymore).

Check out docker and docker-compose docs for more container management utilities.

## Areas for improvement

- Create a Dockerfile to build wise-webapp using an `app.properties` file mounted from this directory (make building easier).
- Simplify this documentation, simplify the process
- Allow to pass configuration as environment variables form docker-compose

If any of this can be improved, please submit a patch or issue.
