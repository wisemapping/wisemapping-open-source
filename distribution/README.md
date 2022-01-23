# What is WiseMapping ?

Wise Mapping is the web mind mapping open source tool that leverages the power of Mind Maps mixing open standards technologies such as SVG and React.
WiseMapping is based on the same code product supporting [http://www.wisemapping.com].

# How to use this image.

There are multiple ways to run WiseMapping depending on your database configuration preference

## Option 1: Running HSQL within the image storage 

> docker run -it --rm -p 8888:8080 wisemapping

Then, open your browser at `http://localhost:8888`. A default user is available for testing `test@wisemapping.com` and password `test`.

***This option, all changes will be lost once the image is stopped. Use it for testing only *** 

## Option 2: Running HSQL with mounted directory

> docker run -it --rm -p 8888:8080 wisemapping

## Option 3: External MySQL/PostgreSQL

### Setup database

### Configure application properties

The next step is configure the WiseMapping for the database and credentials. 
Download `app.properties` configuration file and configure the required sections:

`curl https://bitbucket.org/wisemapping/wisemapping-open-source/raw/644b7078d790220c7844b732a83d45495f11d64e/wise-webapp/src/main/webapp/WEB-INF/app.properties`

### Starting the application

Run the application mounting your previously configured `app.properties`  

> docker run --mount type=bind,source=<your-file-path>/app.properties,target=/usr/local/tomcat/webapps/ROOT/classes/app.properties  -it --rm -p 8888:8080 wisemapping

# Advanced configuration

> docker run --mount type=bind,source=/Users/veigap/repos/wisemapping-open-source/distribution/app.properties,target=/usr/local/tomcat/webapps/ROOT/classes/app.properties  -it --rm -p 8888:8080 wisemapping