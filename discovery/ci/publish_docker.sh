#!/bin/bash

# build multiple times
# see https://github.com/GoogleContainerTools/jib/issues/802

export TAG=`if [ "$TRAVIS_BRANCH" == "master" ]; then echo "latest"; else echo $TRAVIS_BRANCH ; fi`
mvn -Ddocker.tag=${TAG} -Djib.to.auth.username=${DOCKER_USER} -Djib.to.auth.password=${DOCKER_PASS} jib:build

mvn -Ddocker.tag=${COMMIT} -Djib.to.auth.username=${DOCKER_USER} -Djib.to.auth.password=${DOCKER_PASS} jib:build
mvn -Ddocker.tag=travis-${TRAVIS_BUILD_NUMBER} -Djib.to.auth.username=${DOCKER_USER} -Djib.to.auth.password=${DOCKER_PASS} jib:build
