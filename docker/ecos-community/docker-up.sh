export LOCAL_HOST=$(ip addr show docker0 | grep -Po 'inet \K[\d.]+')
export ALFRESCO_TARGET=$LOCAL_HOST:8080
export GATEWAY_TARGET=$LOCAL_HOST:8085
docker-compose pull
docker-compose up
