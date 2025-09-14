@echo off
set JAVA_HOME=C:\Program Files\Java\jdk-17
set PATH=%JAVA_HOME%\bin;%PATH%
echo Configurando Java...
java -version
echo.
echo Ejecutando proyecto Spring Boot...
.\mvnw.cmd spring-boot:run