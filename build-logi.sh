#!/usr/bin/env bash
export MAVEN_OPTS="-Xmx2048m -Xms1024m -Djava.awt.headless=true"
mvn clean install
npm install
grunt development $1
cd modules/api/
mvn assembly:assembly $2
cd ../../
