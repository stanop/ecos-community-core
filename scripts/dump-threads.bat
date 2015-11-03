@echo off
for /f "tokens=1-2" %%i in ('tasklist') do @if %%i == tomcat7.exe set PID=%%j
echo Found Alfresco, PID %PID%

for /f "tokens=2 delims==" %%I in ('wmic os get localdatetime /format:list') do set DATE=%%I
C:\Alfresco\bin\psexec -s C:\Alfresco\java\bin\jstack %PID% > D:\thread-dumps\thread-dump.%DATE%.txt