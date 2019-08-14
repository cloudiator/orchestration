#!/bin/bash

if [ "${SONAR_SCANNER_HOME}" != "" ]; then
   mvn -q org.jacoco:jacoco-maven-plugin:prepare-agent install sonar:sonar -Dmaven.javadoc.skip=true
else
  mvn -q org.jacoco:jacoco-maven-plugin:prepare-agent install -Dmaven.javadoc.skip=true
fi