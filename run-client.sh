#!/bin/sh
echo "Clean target/ dir"
rm -rf target
mkdir target

echo "Compiling client src"
javac -d target src/client/*.java

echo "Creating client.jar from src/client"
jar cmf src/client/Manifest.mf target/client.jar -C target client

echo "Running client"
java -jar target/client.jar -cp ../lib