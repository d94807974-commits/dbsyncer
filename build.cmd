@echo off

echo "Clean Project ..."
call mvn clean -f pom.xml

echo "Build Project ..."
call mvn compile package -f pom.xml -D"maven.test.skip=true"

set CP_PATH=%~dp0
move %CP_PATH%realtime-etl-web\target\realtime-etl-*.zip %CP_PATH%

:exit
