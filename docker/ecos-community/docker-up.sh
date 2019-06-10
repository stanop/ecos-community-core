export ALFRESCO_HOST=$(hostname -I | cut -d' ' -f1):8080
docker-compose pull
docker-compose up
