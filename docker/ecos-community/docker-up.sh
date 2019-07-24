export LOCAL_HOST=$(hostname -I | cut -d' ' -f1)
export ALFRESCO_TARGET=$LOCAL_HOST:8080
export GATEWAY_TARGET=$LOCAL_HOST
docker-compose pull
docker-compose up
