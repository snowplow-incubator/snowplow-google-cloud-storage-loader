#!/bin/bash

tag=$1

file="${HOME}/.dockercfg"
docker_repo="snowplow-docker-registry.bintray.io"
curl -X GET \
    -u${BINTRAY_SNOWPLOW_DOCKER_USER}:${BINTRAY_SNOWPLOW_DOCKER_API_KEY} \
    https://${docker_repo}/v2/auth > $file

cd ${TRAVIS_BUILD_DIR}

project_version=$(sbt -no-colors version | perl -ne 'print "$1\n" if /info.*(\d+\.\d+\.\d+[^\r\n]*)/' | tail -n 1 | tr -d '\n')
if [[ "${tag}" = "${project_version}" ]]; then
    sbt docker:publishLocal
    docker push "${docker_repo}/snowplow/snowplow-google-cloud-storage-loader:${tag}"
else
    echo "Tag version '${tag}' doesn't match version in scala project ('${project_version}'). aborting!"
    exit 1
fi
