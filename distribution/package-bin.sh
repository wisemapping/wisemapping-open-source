#!/bin/bash

set -e	
set -u

WISE_VERSION=$1
BASE_DIR=`pwd`
TARGET_DIR=$BASE_DIR/target
JETTY_DIR=$TARGET_DIR/wisemapping-$WISE_VERSION
WISE_WEBAPP_DIR=$JETTY_DIR/webapps/wisemapping
JETTY_DIST_DIR=jetty-distribution-7.4.3.v20110701
JETTY_ZIP=${JETTY_DIST_DIR}.zip

# Clean ...
mvn -o -f $BASE_DIR/../pom.xml clean
[ ! -e target ] && mkdir target 
rm -fr ${JETTY_DIR}
rm -fr ${TARGET_DIR}/${JETTY_DIST_DIR}

# Prepare resources ..
mvn -o -f $BASE_DIR/../pom.xml install

if [ ! -f ./target/${JETTY_ZIP}  ]
then	
	echo "Download Jetty"
	wget http://download.eclipse.org/jetty/stable-7/dist/${JETTY_ZIP} -P $TARGET_DIR 
fi

echo "Unzip Jetty ...:"  
unzip ${TARGET_DIR}/${JETTY_ZIP}  -d ${TARGET_DIR}/ > /dev/null
mv ${TARGET_DIR}/${JETTY_DIST_DIR} ${JETTY_DIR}
                   
# Clean unsed files ...
rm -r $JETTY_DIR/webapps/*
rm -r $JETTY_DIR/contexts/*

# Now, start wise-webapps customization ...
echo "Unzip wisemappig.war ..."
mkdir $WISE_WEBAPP_DIR
unzip $BASE_DIR/../wise-webapp/target/wisemapping.war -d $WISE_WEBAPP_DIR >/dev/null
rm $WISE_WEBAPP_DIR/images/wisemapping.swf

mkdir $WISE_WEBAPP_DIR/WEB-INF/database
cp -r $BASE_DIR/../wise-webapp/target/db/* $WISE_WEBAPP_DIR/WEB-INF/database/
cp $BASE_DIR/wisemapping.xml $JETTY_DIR/contexts/

# Some replacements ...
sed 's/target\/db\/wisemapping/webapps\/wisemapping\/WEB-INF\/database\/wisemapping/' $WISE_WEBAPP_DIR/WEB-INF/app.properties > $WISE_WEBAPP_DIR/WEB-INF/app.properties2
mv $WISE_WEBAPP_DIR/WEB-INF/app.properties2 $WISE_WEBAPP_DIR/WEB-INF/app.properties


# Distribute scripts
cp -r $BASE_DIR/../wise-webapp/src/test/sql $TARGET_DIR/wisemapping-$WISE_VERSION/config

# Zip all ...
cd $TARGET_DIR
zip -r wisemapping-$WISE_VERSION.zip wisemapping-$WISE_VERSION
cd ..
