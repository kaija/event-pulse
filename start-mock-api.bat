@echo off
REM Start Mock Profile API Server
REM This script compiles and runs the mock server for testing

set PORT=%1
if "%PORT%"=="" set PORT=8080

echo Starting Mock Profile API Server on port %PORT%...
echo.

REM Compile the project if needed
if not exist "target\classes" (
    echo Compiling project...
    call mvn compile
    if errorlevel 1 (
        echo Compilation failed!
        exit /b 1
    )
)

REM Get the classpath
for /f "delims=" %%i in ('mvn dependency:build-classpath -Dmdep.outputFile^=/dev/stdout -q') do set CLASSPATH=%%i

REM Run the mock server
java -cp "target\classes;%CLASSPATH%" com.example.flink.tools.MockProfileApiServer %PORT%
