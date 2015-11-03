@echo off
:: enable multi line strings
setlocal enableDelayedExpansion
:: read properties
call properties.bat

for /f "tokens=2 delims==" %%I in ('wmic os get localdatetime /format:list') do set DATE=%%I

set WORK_DIR=%DATE%
set DIST_DIR=%WORK_DIR%\dist

:: get artefacts
if %UPDATE_REPO% == true (
    mkdir %DIST_DIR%\amps
    for /F %%i in ("!AMPS!") do %DOWNLOAD% "%PATH_TO_ARTIFACTS%/%%i" -o "%DIST_DIR%\amps\%%~nxi"
)

if %UPDATE_SHARE% == true (
    mkdir %DIST_DIR%\amps_share
    for /F %%i in ("!AMPS_SHARE!") do %DOWNLOAD% "%PATH_TO_ARTIFACTS%/%%i" -o "%DIST_DIR%\amps_share\%%~nxi"
)

%STOP_ALFRESCO%
timeout 60
%EMERGENCY_STOP_ALFRESCO%
echo service alfresco stopped

if %ENABLE_BACKUP% == true (
	echo Backup starting...
	echo Target directory: %BACKUP_DIR%\%DATE%
	mkdir %BACKUP_DIR%\%DATE%
	%zip% a -tzip %BACKUP_DIR%\%DATE%\alfresco.zip %ALFRESCO_DIR%
	echo "Backup finished."
)

echo Modules installation starting...
echo Modules directory: %DIST_DIR%
echo Webapps directory: %CATALINA_HOME%\webapps
if %UPDATE_REPO% == true (
    for /F %%i in ("!AMPS!") do (
        echo Applying "%DIST_DIR%/amps/%%~nxi"...
        %JAVA% -jar %ALFRESCO_DIR%/bin/alfresco-mmt.jar install "%DIST_DIR%/amps/%%~nxi" "%CATALINA_HOME%/webapps/alfresco.war" -force
    )
    %JAVA% -jar %ALFRESCO_DIR%/bin/alfresco-mmt.jar list %CATALINA_HOME%/webapps/alfresco.war
)
if %UPDATE_SHARE% == true (
    for /F %%i in ("!AMPS_SHARE!") do (
        echo Applying "%DIST_DIR%/amps_share/%%~nxi"...
        %JAVA% -jar %ALFRESCO_DIR%/bin/alfresco-mmt.jar install "%DIST_DIR%/amps_share/%%~nxi" "%CATALINA_HOME%/webapps/share.war" -force
    )
    %JAVA% -jar %ALFRESCO_DIR%/bin/alfresco-mmt.jar list %CATALINA_HOME%/webapps/share.war
)
echo Modules installation finished.

echo Cleaning out temporary files starting...
if %UPDATE_REPO% == true (
    rd /s /q %CATALINA_HOME%\webapps\alfresco
)
if %UPDATE_SHARE% == true (
    rd /s /q %CATALINA_HOME%\webapps\share
)
del /f /q %CATALINA_HOME%\webapps\*.bak
echo Cleaning out temporary files finished.

%START_ALFRESCO%
echo service alfresco started
