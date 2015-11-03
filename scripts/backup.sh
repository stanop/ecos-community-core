#!/bin/bash

# read properties
. `echo "$0" | sed 's/\/\([^\/]*\)$/\/properties.sh/'`

export DATE=$(date +%Y%m%d-%H%M)

WORK_DIR=$DATE

$ALFRESCO_DIR/alfresco.sh stop
echo "service alfresco stopped"

echo "Backup starting..."
echo "Target directory: $BACKUP_DIR/$DATE"
mkdir -p $BACKUP_DIR/$DATE

PGPASSWORD=$DB_USER_PASSWORD

#pg_dump -U alfresco -h localhost -p 5432 > alfresco_pecom_dump.dump
pg_dump -U $DB_USER_NAME -h $DB_HOST -p $DB_PORT $DB_NAME > $BACKUP_DIR/$DATE/$DB_NAME_dump.dump
echo backup alf_data
tar -cvzf $BACKUP_DIR/$DATE/alf_data.tar.gz $ALFRESCO_DATA_DIR
echo "Backup finished."

$ALFRESCO_DIR/alfresco.sh start
echo "service alfresco started"
