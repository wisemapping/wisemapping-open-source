#!/bin/bash

set -e	
set -u

WISE_VERSION=$1
BASE_DIR=`pwd`
TARGET_DIR=$BASE_DIR/target
JETTY_DIR=$TARGET_DIR/wisemapping-$WISE_VERSION
WISE_WEBAPP_DIR=$JETTY_DIR/webapps/wisemapping
JETTY_VERSION=8.1.14.v20131031
JETTY_DIST_DIR=jetty-distribution-${JETTY_VERSION}
JETTY_ZIP=${JETTY_DIST_DIR}.zip

# Clean ...
mvn -o -f $BASE_DIR/../pom.xml clean
[ ! -e target ] && mkdir target 
rm -fr ${JETTY_DIR}
rm -fr ${TARGET_DIR}/${JETTY_DIST_DIR}

# Prepare resources ..
mvn -o -f $BASE_DIR/../pom.xml package -Dmaven.test.skip=true

if [ ! -f ./target/${JETTY_ZIP}  ]
then	
	echo "Download Jetty"
	wget http://download.eclipse.org/jetty/${JETTY_VERSION}/dist/${JETTY_ZIP} -P $TARGET_DIR 
fi

echo "Unzip Jetty ...:"  
unzip ${TARGET_DIR}/${JETTY_ZIP}  -d ${TARGET_DIR}/ > /dev/null
mv ${TARGET_DIR}/${JETTY_DIST_DIR} ${JETTY_DIR}

# Clean unsed files ...
rm -rf $JETTY_DIR/webapps/*
rm -rf $JETTY_DIR/contexts/*
rm -rf $JETTY_DIR/javadoc

# Now, start wise-webapps customization ...
echo "Unzip wisemappig.war ..."
mkdir $WISE_WEBAPP_DIR
unzip $BASE_DIR/../wise-webapp/target/wisemapping.war -d $WISE_WEBAPP_DIR >/dev/null

# DB Configuration ...
sed 's/\${database.base.url}\/db\/wisemapping/webapps\/wisemapping\/WEB-INF\/database\/wisemapping/' $WISE_WEBAPP_DIR/WEB-INF/app.properties > $WISE_WEBAPP_DIR/WEB-INF/app.properties2
mv $WISE_WEBAPP_DIR/WEB-INF/app.properties2 $WISE_WEBAPP_DIR/WEB-INF/app.properties

mkdir $WISE_WEBAPP_DIR/WEB-INF/database
cp -r $BASE_DIR/../wise-webapp/target/db/* $WISE_WEBAPP_DIR/WEB-INF/database/
cp $BASE_DIR/wisemapping.xml $JETTY_DIR/contexts/


# Distribute scripts
cp -r $BASE_DIR/../config/ $TARGET_DIR/wisemapping-$WISE_VERSION/config
cp ./start.sh ${JETTY_DIR}/
cp -r $BASE_DIR/service $TARGET_DIR/wisemapping-$WISE_VERSION/service

# Store version
echo $1 > $WISE_WEBAPP_DIR/version
git rev-parse HEAD >> $WISE_WEBAPP_DIR/version

# Zip all ...
cd $TARGET_DIR
zip -r wisemapping-$WISE_VERSION.zip wisemapping-$WISE_VERSION
cd ..
