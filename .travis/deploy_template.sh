#!/bin/bash

tag=$1

export GOOGLE_APPLICATION_CREDENTIALS="${HOME}/service-account.json"

cd ${TRAVIS_BUILD_DIR}

project_version=$(sbt -no-colors version | perl -ne 'print "$1\n" if /info.*(\d+\.\d+\.\d+[^\r\n]*)/' | tail -n 1 | tr -d '\n')
if [[ "${tag}" = *"${project_version}" ]]; then
    sbt "runMain com.snowplowanalytics.storage.googlecloudstorage.loader.CloudStorageLoader --project=snowplow-assets \
      --templateLocation=gs://sp-hosted-assets/4-storage/snowplow-google-cloud-storage-loader/${tag}/SnowplowGoogleCloudStorageLoaderTemplate-${tag} \
      --stagingLocation=gs://sp-hosted-assets/4-storage/snowplow-google-cloud-storage-loader/${tag}/staging \
      --runner=DataflowRunner \
      --tempLocation=gs://sp-hosted-assets/tmp \
      --autoscalingAlgorithm=THROUGHPUT_BASED \
      --numWorkers=1 \
      --numShards=1 \
      --diskSizeGb=30 \
      --workerMachineType=n1-standard-1"
else
    echo "Tag version '${tag}' doesn't match version in scala project ('${project_version}'). aborting!"
    exit 1
fi
