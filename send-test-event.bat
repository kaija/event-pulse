@echo off
REM Send Test Event Script for Windows
REM This script sends test events to Kafka using the KafkaEventProducer tool
REM
REM Usage:
REM   send-test-event.bat [json-file-or-directory] [delay-ms]
REM
REM Examples:
REM   send-test-event.bat test-data\sample-event-1.json
REM   send-test-event.bat test-data\ 1000

setlocal

REM Default configuration
if "%KAFKA_BOOTSTRAP_SERVERS%"=="" set KAFKA_BOOTSTRAP_SERVERS=localhost:9092
if "%KAFKA_TOPIC%"=="" set KAFKA_TOPIC=user-tracking-events
set JAR_FILE=target\flink-event-trigger-1.0-SNAPSHOT.jar

REM Check if JAR file exists
if not exist "%JAR_FILE%" (
    echo Error: JAR file not found: %JAR_FILE%
    echo Please build the project first: mvn clean package
    exit /b 1
)

REM Check arguments
if "%~1"=="" (
    echo Usage: %0 ^<json-file-or-directory^> [delay-ms]
    echo.
    echo Examples:
    echo   %0 test-data\sample-event-1.json
    echo   %0 test-data\ 1000
    echo.
    echo Environment variables:
    echo   KAFKA_BOOTSTRAP_SERVERS ^(default: localhost:9092^)
    echo   KAFKA_TOPIC ^(default: user-tracking-events^)
    exit /b 1
)

set PATH_ARG=%~1
set DELAY_MS=%~2
if "%DELAY_MS%"=="" set DELAY_MS=0

REM Check if path exists
if not exist "%PATH_ARG%" (
    echo Error: Path does not exist: %PATH_ARG%
    exit /b 1
)

echo === Kafka Event Producer ===
echo Bootstrap Servers: %KAFKA_BOOTSTRAP_SERVERS%
echo Topic: %KAFKA_TOPIC%
echo Path: %PATH_ARG%
if not "%DELAY_MS%"=="0" echo Delay: %DELAY_MS%ms between events
echo.

REM Run the producer
echo Sending events...
java -cp "%JAR_FILE%" com.example.flink.tools.KafkaEventProducer "%KAFKA_BOOTSTRAP_SERVERS%" "%KAFKA_TOPIC%" "%PATH_ARG%" "%DELAY_MS%"

if %errorlevel% equ 0 (
    echo Events sent successfully
) else (
    echo Failed to send events
    exit /b 1
)

endlocal
