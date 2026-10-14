@echo off
set JAVA_HOME=C:\Program Files\Java\jdk-25
set PATH=%JAVA_HOME%\bin;C:\tools\maven\bin;%PATH%
C:\tools\maven\bin\mvn.cmd spring-boot:run
