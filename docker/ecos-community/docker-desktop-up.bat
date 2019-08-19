SET LOCAL_HOST=host.docker.internal
SET ALFRESCO_TARGET=%LOCAL_HOST%:8080
SET GATEWAY_TARGET=%LOCAL_HOST%:8085
docker-compose pull
docker-compose up
