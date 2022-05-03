#!/bin/sh
echo "Clean target/ dir"
rm -rf target
mkdir target

echo "Compiling server src"
javac -d target src/server/*.java

echo "Creating server.jar from src/server"
jar cmf src/server/Manifest.mf target/server.jar logging.properties -C target server

echo "Running server"
java -Djava.util.logging.config.file=logging.properties -jar target/server.jar -cp ../lib 
