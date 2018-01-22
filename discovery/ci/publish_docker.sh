#!/bin/bash

docker login -u $DOCKER_USER -p $DOCKER_PASS
export REPO=cloudiator/discovery-agent
export TAG=`if [ "$TRAVIS_BRANCH" == "master" ]; then echo "latest"; else echo $TRAVIS_BRANCH ; fi`
docker build -t $REPO:$COMMIT discovery
docker tag $REPO:$COMMIT $REPO:$TAG
docker tag $REPO:$COMMIT $REPO:travis-$TRAVIS_BUILD_NUMBER
docker push $REPO
