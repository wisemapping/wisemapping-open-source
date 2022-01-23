 #!/bin/sh

set -o
set -u

docker build -t wisemapping -f ./Dockerfile ../wise-webapp/target/