for /f "delims=[] tokens=2" %%a in ('ping -4 -n 1 %ComputerName% ^| findstr [') do SET LOCAL_HOST=%%a
SET ALFRESCO_TARGET=%LOCAL_HOST%:8080
SET GATEWAY_TARGET=%LOCAL_HOST%:8085
REM docker-compose pull
REM docker-compose up
