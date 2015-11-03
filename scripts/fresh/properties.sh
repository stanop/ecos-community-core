ALFRESCO_DIR=/opt/alfresco-4.2.f
JAVA=$ALFRESCO_DIR/java/bin/java
BACKUP_DIR=/opt/backup
ARTIFACTORY_USER=citeck
ARTIFACTORY_PASS=Qq123456
ARTIFACTORY_SERVER=http://ec2-54-246-72-179.eu-west-1.compute.amazonaws.com
ARTIFACT_DIR=dist/idocs/distr
PATH_TO_ARTIFACTS=$ARTIFACTORY_SERVER/$ARTIFACT_DIR

PROXY_SERVER=http://proxy-server:port
PROXY_USER=user
PROXY_PASS=pass
PROXY_OPTS=--proxy-negotiate

CATALINA_HOME=$ALFRESCO_DIR/tomcat
ENABLE_BACKUP=false

DB_NAME=alfresco
DB_USER_NAME=postgres
DB_USER_PASSWORD="tiny room drive"
DB_HOST=localhost
DB_PORT=5432
PSQL="$ALFRESCO_DIR/postgresql/bin/psql"

FRESH_INSTALL=true
ALF_DATA=$ALFRESCO_DIR/alf_data
CONTENTSTORE_FILES_PATTERN="$ALF_DATA/contentstore*"
INDICES_FILES_PATTERN="$ALF_DATA/*lucene-indices $ALF_DATA/solr/archive/SpacesStore/* $ALF_DATA/solr/archive-SpacesStore/alfrescoModels/* $ALF_DATA/solr/workspace/SpacesStore/* $ALF_DATA/solr/workspace-SpacesStore/alfrescoModels/*"
ORIGINAL_WARS=/opt/distr/alfresco-community-4.2.f/web-server/webapps/*.war

DISTRIB_PATH=ecos-contracts/citeck-ecos-contracts-2.0-distr.zip
DISTRIB_URL=$ARTIFACTORY_SERVER/$ARTIFACT_DIR/$DISTRIB_PATH
DISTRIB_INSTALLER=install-ecos.sh

WGET="/usr/bin/wget --user=$ARTIFACTORY_USER --password=$ARTIFACTORY_PASS -O "
CURL_PROXY="/usr/bin/curl --proxy $PROXY_SERVER --proxy-user $PROXY_USER:$PROXY_PASS $PROXY_OPTS --user $ARTIFACTORY_USER:$ARTIFACTORY_PASS -o "
CURL="/usr/bin/curl --user $ARTIFACTORY_USER:$ARTIFACTORY_PASS -o "

DOWNLOAD=$WGET

