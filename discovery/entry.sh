#!/bin/sh

set -x

echo ${MYSQL_USER}

# Run the service
java -jar discovery-agent.jar

