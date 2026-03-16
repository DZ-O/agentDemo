@echo off
set "JAR_FILE=target\openbutler-1.0.0.jar"

if not exist "%JAR_FILE%" (
    echo JAR file not found. Building...
    call mvn clean package -DskipTests
)

if "%OPENAI_API_KEY%"=="" (
    echo Warning: OPENAI_API_KEY environment variable is not set.
    echo Please set it or configure it in application.yml
)

echo Starting OpenButler...
java -jar "%JAR_FILE%"
