#!/bin/bash

#set -x	
WISE_VERSION=0.94
BASE_DIR=`pwd`
TARGET_DIR=$BASE_DIR/target
JETTY_DIR=$TARGET_DIR/wisemapping-$WISE_VERSION
WISE_WEBAPP_DIR=$JETTY_DIR/webapps/wisemapping

# Clean ...
mvn -f $BASE_DIR/../pom.xml clean
mkdir target 2>/dev/null
rm -fr $JETTY_DIR
rm -fr $TARGET_DIR/jetty-hightide-7.0.0.v20091005

# Prepare resources ..
mvn -f $BASE_DIR/../pom.xml install

if [ ! -f ./target/jetty-hightide-7.0.0.v20091005.zip  ]
then	
	echo "Download Jetty"
	wget http://dist.codehaus.org/jetty/jetty-7.0.0/jetty-hightide-7.0.0.v20091005.zip -P $TARGET_DIR 
fi

echo "Unzip Jetty ...:"  
unzip $TARGET_DIR/jetty-hightide-7.0.0.v20091005.zip -d $TARGET_DIR/ > /dev/null
mv $TARGET_DIR/jetty-hightide-7.0.0.v20091005 $JETTY_DIR
                   

# Clean unsed files ...
rm -r $JETTY_DIR/webapps/*
rm -r $JETTY_DIR/contexts/*

# Now, start wise-webapps customization ...
echo "Unzip wisemappig.war ..."
mkdir $WISE_WEBAPP_DIR
unzip $BASE_DIR/../wise-webapp/target/wisemapping.war -d $WISE_WEBAPP_DIR >/dev/null
rm $WISE_WEBAPP_DIR/images/wisemapping.swf

mkdir $WISE_WEBAPP_DIR/WEB-INF/database
cp -r $BASE_DIR/../wise-webapp/target/db $WISE_WEBAPP_DIR/WEB-INF/database
cp $BASE_DIR/wisemapping.xml $JETTY_DIR/contexts/

# Some replacements ...
sed 's/target\/db\/wisemapping/webapps\/wisemapping\/WEB-INF\/database\/wisemapping/' $WISE_WEBAPP_DIR/WEB-INF/app.properties > $WISE_WEBAPP_DIR/WEB-INF/app.properties2
mv $WISE_WEBAPP_DIR/WEB-INF/app.properties2 $WISE_WEBAPP_DIR/WEB-INF/app.properties

#Build final Zip
cd $TARGET_DIR
zip -r wisemapping-$WISE_VERSION.zip wisemapping-$WISE_VERSION
cd ..
