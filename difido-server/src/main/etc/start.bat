@echo off
cd ..
rem set DEBUG=-agentlib:jdwp=transport=dt_socket,server=y,address=8000,suspend=n
java %DEBUG%  -Xms256m -Xmx1024m -jar -Dserver.address=0.0.0.0 -Dserver.port=8080 -Dlogging.level.org.springframework.web=ERROR -Dlogging.level.il.co.topq.report=ERROR lib/difido-server.jar