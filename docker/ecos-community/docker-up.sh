export LOCAL_HOST=$(/sbin/ip route|awk '/default/ { print $3 }')
export ALFRESCO_TARGET=$LOCAL_HOST:8080
export GATEWAY_TARGET=$LOCAL_HOST:8085
docker-compose pull
docker-compose up
