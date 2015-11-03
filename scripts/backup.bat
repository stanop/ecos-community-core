
@echo off
:: enable multi line strings
setlocal enableDelayedExpansion
:: read properties
call properties.bat

for /f "tokens=2 delims==" %%I in ('wmic os get localdatetime /format:list') do set DATE=%%I

%STOP_ALFRESCO%
timeout 20
%EMERGENCY_STOP_ALFRESCO%
echo service alfresco stopped

echo Backup starting...
echo Target directory: %BACKUP_DIR%\%DATE%
echo alfresco directory: %ALFRESCO_DATA_DIR%
mkdir %BACKUP_DIR%\%DATE%
set PGPASSWORD=%DB_USER_PASSWORD%

%START_Postgre%
%ALFRESCO_DIR%\postgresql\bin\pg_dump -Fc -U %DB_USER_NAME% -h %DB_HOST% -p %DB_PORT% %DB_NAME% > %BACKUP_DIR%\%DATE%\%DB_NAME%_dump.dump

%STOP_Postgre%
timeout 20
%EMERGENCY_STOP_Postgre%
echo service Postgre stopped

%zip% a -tzip %BACKUP_DIR%\%DATE%\alf_data.zip %ALFRESCO_DATA_DIR%
echo "Backup finished."
