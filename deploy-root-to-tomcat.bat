@echo off
setlocal

set "PROJECT_DIR=%~dp0"
set "FRONTEND_DIR=%PROJECT_DIR%src\frontend\Code for UI"
set "TOMCAT_WEBAPPS=C:\Users\gglor\OneDrive\Desktop\school software\workplace\apache-tomcat-10.1.36\webapps"

echo Building React frontend...
pushd "%FRONTEND_DIR%" || exit /b 1
call npm.cmd run build || exit /b 1
popd

echo Building Java WAR...
pushd "%PROJECT_DIR%" || exit /b 1
call mvn.cmd clean package || exit /b 1
popd

echo Copying ROOT.war to Tomcat...
copy /Y "%PROJECT_DIR%target\ROOT.war" "%TOMCAT_WEBAPPS%\ROOT.war" || exit /b 1

echo Done. Tomcat may take a few seconds to redeploy ROOT.war.
endlocal
