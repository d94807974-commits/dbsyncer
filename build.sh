#!/bin/bash

mvn clean -f pom.xml
mvn compile package -f pom.xml -Dmaven.test.skip=true

CURRENT_DIR=$(pwd)
mv "$CURRENT_DIR"/realtime-etl-web/target/realtime-etl-*.zip "$CURRENT_DIR"/
