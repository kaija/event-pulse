#!/bin/bash

# Start Mock Profile API Server
# This script compiles and runs the mock server for testing

PORT=${1:-8080}

echo "Starting Mock Profile API Server on port $PORT..."
echo ""

# Compile the project if needed
if [ ! -d "target/classes" ]; then
    echo "Compiling project..."
    mvn compile
    if [ $? -ne 0 ]; then
        echo "Compilation failed!"
        exit 1
    fi
fi

# Run the mock server
java -cp "target/classes:$(mvn dependency:build-classpath -Dmdep.outputFile=/dev/stdout -q)" \
    com.example.flink.tools.MockProfileApiServer $PORT
