@echo off

set current_dir=%~dp0\..
cd %current_dir%

set _JAVACMD=java.exe
if exist "%JAVA_HOME%\bin\java.exe" set _JAVACMD=%JAVA_HOME%\bin\java.exe

set HOST=0.0.0.0
set PORT=8080
set LOGGING_LEVEL=WARN
rem set DEBUG=-agentlib:jdwp=transport=dt_socket,server=y,address=8000,suspend=n

"%_JAVACMD%" %DEBUG% -Xms256m -Xmx1024m -Dserver.address=%HOST% -Dserver.port=%PORT% -Dlogging.level.org.springframework.web=ERROR -Dlogging.level.il.co.topq.report=%LOGGING_LEVEL% -cp "lib\difido-server.jar;plugin\*" org.springframework.boot.loader.JarLauncher