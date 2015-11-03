#!/bin/bash

# read properties
. `echo "$0" | sed 's/\/\([^\/]*\)$/\/properties.sh/'`

export DATE=$(date +%Y%m%d-%H%M)

WORK_DIR=$DATE
DIST_DIR="$WORK_DIR/dist"

# get artifacts
if [[ $UPDATE_REPO == "true" ]]; then
    mkdir -p "$DIST_DIR/amps"
    for AMP in $AMPS; do
        echo $DOWNLOAD "$DIST_DIR/amps/`basename $AMP`" "$PATH_TO_ARTIFACTS/$AMP"
        $DOWNLOAD "$DIST_DIR/amps/`basename $AMP`" "$PATH_TO_ARTIFACTS/$AMP"
    done
fi

if [[ $UPDATE_SHARE == "true" ]]; then
    mkdir -p "$DIST_DIR/amps_share"
    for AMP in $AMPS_SHARE; do
        echo "loading $PATH_TO_ARTIFACTS/$AMP"
        $DOWNLOAD "$DIST_DIR/amps_share/`basename $AMP`" "$PATH_TO_ARTIFACTS/$AMP"
    done
fi

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

echo "Modules installation starting..."
echo "Modules directory: $DIST_DIR"
echo "Webapps directory: $CATALINA_HOME/webapps"
if [[ $UPDATE_REPO == "true" ]]; then
    for AMP in $AMPS; do
        echo "installing `basename $AMP`"
        $JAVA -jar $ALFRESCO_DIR/bin/alfresco-mmt.jar install "$DIST_DIR/amps/`basename $AMP`" "$CATALINA_HOME/webapps/alfresco.war" -force
    done
    $JAVA -jar $ALFRESCO_DIR/bin/alfresco-mmt.jar list $CATALINA_HOME/webapps/alfresco.war
fi
if [[ $UPDATE_SHARE == "true" ]]; then
    for AMP in $AMPS_SHARE; do
        echo "installing `basename $AMP`"
        $JAVA -jar $ALFRESCO_DIR/bin/alfresco-mmt.jar install "$DIST_DIR/amps_share/`basename $AMP`" "$CATALINA_HOME/webapps/share.war" -force
    done
    $JAVA -jar $ALFRESCO_DIR/bin/alfresco-mmt.jar list $CATALINA_HOME/webapps/share.war
fi
echo "Modules installation finished."

echo "Cleaning out temporary files starting..."
if [[ $UPDATE_REPO == "true" ]]; then
    rm -rf $CATALINA_HOME/webapps/alfresco
fi
if [[ $UPDATE_SHARE == "true" ]]; then
    rm -rf $CATALINA_HOME/webapps/share
fi
. $ALFRESCO_DIR/bin/clean_tomcat.sh
rm $CATALINA_HOME/webapps/*.bak
echo "Cleaning out temporary files finished."

$ALFRESCO_DIR/alfresco.sh start
echo service alfresco started
