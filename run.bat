@ECHO OFF

IF "%MAVEN_OPTS%" == "" (
    ECHO The environment variable 'MAVEN_OPTS' is not set, setting it for you
    SET MAVEN_OPTS=-Xms256m -Xmx2G -XX:PermSize=300m -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=8000
)
ECHO MAVEN_OPTS is set to '%MAVEN_OPTS%'
mvn clean install ecos:run -DskipTests=true -Pdevelopment
