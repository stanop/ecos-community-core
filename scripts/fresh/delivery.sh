#!/bin/bash

# read properties
. `echo "$0" | sed 's/\/\([^\/]*\)$/\/properties.sh/'`

export DATE=$(date +%Y%m%d-%H%M)

WORK_DIR=$DATE
DIST_DIR="$WORK_DIR/dist"

# get artifacts
mkdir -p $DIST_DIR
$DOWNLOAD $DIST_DIR/distrib.zip "$DISTRIB_URL"


$ALFRESCO_DIR/alfresco.sh stop tomcat
echo service alfresco stopped

if [[ $ENABLE_BACKUP == "true" ]];
then
    echo "Backup starting..."
    echo "Target directory: $BACKUP_DIR/$DATE"
    mkdir -p $BACKUP_DIR/$DATE
    tar -cvzf $BACKUP_DIR/$DATE/alfresco.tar.gz $ALFRESCO_DIR
    echo "Backup finished."
fi

if [[ $FRESH_INSTALL == "true" ]];
then
	echo Cleaning contentstore...
	rm -rf $CONTENTSTORE_FILES_PATTERN 
	echo Cleaning indices...
	rm -rf $INDICES_FILES_PATTERN
	echo Cleaning database...
	PGPASSWORD="$DB_USER_PASSWORD" $PSQL -U $DB_USER_NAME -p $DB_PORT << EOF
DROP DATABASE $DB_NAME;
CREATE DATABASE $DB_NAME;
GRANT ALL ON DATABASE $DB_NAME TO alfresco;
EOF
	echo Cleaning AMP modules...
	rm -rf $ALFRESCO_DIR/amps/* $ALFRESCO_DIR/amps_share/*
	echo Cleaning classpath...
	rm -rf $CATALINA_HOME/shared/classes/alfresco
	echo Cleaning webapps...
	rm -rf $CATALINA_HOME/webapps/alfresco.war
	rm -rf $CATALINA_HOME/webapps/share.war
	cp $ORIGINAL_WARS $CATALINA_HOME/webapps
fi

echo "Distribution installation..."
unzip -o $DIST_DIR/distrib.zip -d $ALFRESCO_DIR
(cd $ALFRESCO_DIR; sh $DISTRIB_INSTALLER)

$ALFRESCO_DIR/alfresco.sh start
echo service alfresco started
