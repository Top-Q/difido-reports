@echo off
cd ..
rem set DEBUG=-agentlib:jdwp=transport=dt_socket,server=y,address=8000,suspend=n
java %DEBUG% -jar -Dserver.address=0.0.0.0 -Dserver.port=8080 lib/difido-server.jar