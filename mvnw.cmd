@echo off
setlocal enabledelayedexpansion

if "%JAVA_HOME%"=="" (
    echo Error: JAVA_HOME is not set. >&2
    exit /b 1
)

if not exist "%JAVA_HOME%\bin\java.exe" (
    echo Error: JAVA_HOME is set to an invalid directory: %JAVA_HOME% >&2
    exit /b 1
)

set "MAVEN_PROJECTBASEDIR=%~dp0"
set "WRAPPER_JAR=%MAVEN_PROJECTBASEDIR%.mvn\wrapper\maven-wrapper.jar"
set "WRAPPER_LAUNCHER=org.apache.maven.wrapper.MavenWrapperMain"
set "MAVEN_JAVA_EXE=%JAVA_HOME%\bin\java.exe"

@REM Download wrapper jar if missing
if not exist "%WRAPPER_JAR%" (
    echo Downloading maven-wrapper.jar ...
    "%MAVEN_JAVA_EXE%" -cp "" -Djava.net.useSystemProxies=true ^
        org.apache.maven.wrapper.MavenWrapperDownloader ^
        "https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.2.0/maven-wrapper-3.2.0.jar" ^
        "%WRAPPER_JAR%"

    if not exist "%WRAPPER_JAR%" (
        powershell -Command "[Net.ServicePointManager]::SecurityProtocol=[Net.SecurityProtocolType]::Tls12; (New-Object Net.WebClient).DownloadFile('https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.2.0/maven-wrapper-3.2.0.jar', '%WRAPPER_JAR%')"
    )
)

"%MAVEN_JAVA_EXE%" ^
  -classpath "%WRAPPER_JAR%" ^
  "-Dmaven.multiModuleProjectDirectory=%MAVEN_PROJECTBASEDIR%" ^
  %WRAPPER_LAUNCHER% %*

exit /b %ERRORLEVEL%
