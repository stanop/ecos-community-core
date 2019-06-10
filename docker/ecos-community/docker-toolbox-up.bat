for /f "delims=[] tokens=2" %%a in ('ping -4 -n 1 %ComputerName% ^| findstr [') do set ALFRESCO_HOST=%%a:8080
docker-compose pull
docker-compose up
