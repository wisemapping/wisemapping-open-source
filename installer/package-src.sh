#!/bin/bash

set -e	
set -u

WISE_VERSION=$1
TMP_DIR=/tmp/wise-src/wisemapping-src
TAR_FILE_NAME=wisemapping-${WISE_VERSION}-src.tar.gz
OUTPUT_DIR=`pwd`"/target"
OUTPUT_FILE=${OUTPUT_DIR}/${TAR_FILE_NAME}


# Clean all.
cd ..
rm -rf ${TMP_DIR}/../
mvn clean 

# Prepare copy 
mkdir -p ${TMP_DIR}
rsync -aCv --exclude ".git"  --exclude "wisemapping.i*" --exclude "**/*/Brix*" --exclude "**/brix" --exclude "*/*.iml" --exclude "*/wisemapping.log*" --exclude "**/.DS_Store" --exclude "*.textile" --exclude "**/.gitignore" --exclude "installer" --exclude "*/target" . ${TMP_DIR}

# Zip file
[ ! -e ${OUTPUT_DIR} ] && mkdir ${OUTPUT_DIR}
rm -f ${OUTPUT_FILE}

cd ${TMP_DIR}/..
tar -cvzf ${OUTPUT_FILE} .

echo 
echo "#################################################################"
echo "Zip file generated on:"${OUTPUT_FILE}
echo "#################################################################"
