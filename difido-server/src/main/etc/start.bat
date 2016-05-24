@echo off
cd ..
rem set DEBUG=-agentlib:jdwp=transport=dt_socket,server=y,address=8000,suspend=n
java %DEBUG% -Xms256m -Xmx1024m -Dserver.address=$HOST -Dserver.port=$PORT -Dlogging.level.org.springframework.web=ERROR -Dlogging.level.il.co.topq.report=$LOGGING_LEVEL -cp "lib/difido-server.jar:plugin/*" org.springframework.boot.loader.JarLauncher