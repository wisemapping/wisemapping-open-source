 #!/bin/sh

set -o
set -u

mvn -f ../pom.xml clean package
#docker build --platform linux/amd64 -t wisemapping/wisemapping:latest -f ./Dockerfile ../wise-webapp/target/
docker buildx create --use --platform=linux/arm64,linux/amd64 --name multi-platform-builder
docker buildx inspect --bootstrap
docker buildx build --platform=linux/amd64,linux/arm64 --push -t wisemapping/wisemapping:latest -f ./Dockerfile ../wise-webapp/target/ 
