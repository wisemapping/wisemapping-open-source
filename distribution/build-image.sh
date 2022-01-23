 #!/bin/sh

set -o
set -u

mvn -f ../pom.xml clean package
docker build -t veigap/wisemapping:latest -f ./Dockerfile ../wise-webapp/target/