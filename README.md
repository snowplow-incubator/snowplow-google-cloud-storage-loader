# Snowplow Google Cloud Storage Loader

## Introduction

Cloud Storage Loader is a [Dataflow][dataflow] job which dumps events from an input
[PubSub][pubsub] subscription into a [Cloud Storage][storage] bucket.

## Building

### Cloud Dataflow template

Cloud Storage Loader is compatible with [Dataflow templates][templates] which gives you
additional flexibility when running your pipeline.

To upload the template to your own bucket, run:

```bash
sbt "runMain com.snowplowanalytics.storage.googlecloudstorage.loader.CloudStorageLoader \
  --project=[PROJECT] \
  --templateLocation=gs://[BUCKET]/SnowplowGoogleCloudStorageLoaderTemplate \
  --stagingLocation=gs://[BUCKET]/staging \
  --runner=DataflowRunner \
  --tempLocation=gs://[BUCKET]/tmp" \
  --numShards=1
```

### Zip archive

To build the zip archive, run:

```bash
sbt universal:packageBin
```

### Docker image

To build a Docker image, run:

```bash
sbt docker:publishLocal
```

## Running

### Through the template

You can run Dataflow templates using a variety of means:

- Using the GCP console
- Using `gcloud`
- Using the REST API

Refer to [the documentation on executing templates][executing-templates] to know more.

Here, we provide an example using `gcloud`:

```bash
gcloud dataflow jobs run [JOB-NAME] \
  --gcs-location gs://sp-hosted-assets/4-storage/snowplow-google-cloud-storage-loader/0.2.0/SnowplowGoogleCloudStorageLoaderTemplate-0.2.0 \
  --parameters \
    inputSubscription=projects/[PROJECT]/subscriptions/[SUBSCRIPTION],\
    outputDirectory=gs://[BUCKET]/YYYY/MM/dd/HH/,\ # partitions by date
    outputFilenamePrefix=output,\ # optional
    shardTemplate=-W-P-SSSSS-of-NNNNN,\ # optional
    outputFilenameSuffix=.txt,\ # optional
    windowDuration=5,\ # optional, in minutes
    compression=none,\ # optional, gzip, bz2 or none
    numShards=1 # optional
```

### Through the zip archive

You can find the archive hosted on [our Bintray][bintray].

Once unzipped the artifact can be run as follows:

```bash
./bin/snowplow-google-cloud-storage-loader \
  --runner=DataFlowRunner \
  --project=[PROJECT] \
  --streaming=true \
  --zone=europe-west2-a \
  --inputSubscription=projects/[PROJECT]/subscriptions/[SUBSCRIPTION] \
  --outputDirectory=gs://[BUCKET]/YYYY/MM/dd/HH/ \ # partitions by date
  --outputFilenamePrefix=output \ # optional
  --shardTemplate=-W-P-SSSSS-of-NNNNN \ # optional
  --outputFilenameSuffix=.txt \ # optional
  --windowDuration=5 \ # optional, in minutes
  --compression=none \ # optional, gzip, bz2 or none
  --numShards=1 # optional
```

To display the help message:

```bash
./bin/snowplow-google-cloud-storage-loader --help
```

To display documentation about Cloud Storage Loader-specific options:

```bash
./bin/snowplow-google-cloud-storage-loader --help=com.snowplowanalytics.storage.googlecloudstorage.loader.Options
```

### Through a docker container

You can also find the image on [our Bintray][bintray-docker].

A container can be run as follows:

```bash
docker run \
  -v $PWD/config:/snowplow/config \
  -e GOOGLE_APPLICATION_CREDENTIALS=/snowplow/config/credentials.json \ # if running outside GCP
  snowplow-docker-registry.bintray.io/snowplow/snowplow-google-cloud-storage-loader:0.2.0 \
  --runner=DataFlowRunner \
  --jobName=[JOB-NAME] \
  --project=[PROJECT] \
  --streaming=true \
  --zone=[ZONE] \
  --inputSubscription=projects/[PROJECT]/subscriptions/[SUBSCRIPTION] \
  --outputDirectory=gs://[BUCKET]/YYYY/MM/dd/HH/ \ # partitions by date
  --outputFilenamePrefix=output \ # optional
  --shardTemplate=-W-P-SSSSS-of-NNNNN \ # optional
  --outputFilenameSuffix=.txt \ # optional
  --windowDuration=5 \ # optional, in minutes
  --compression=none \ # optional, gzip, bz2 or none
  --numShards=1 # optional
```

To display the help message:

```bash
docker run snowplow-docker-registry.bintray.io/snowplow/snowplow-google-cloud-storage-loader:0.2.0 \
  --help
```

To display documentation about Cloud Storage Loader-specific options:

```bash
docker run snowplow-docker-registry.bintray.io/snowplow/snowplow-google-cloud-storage-loader:0.2.0 \
  --help=com.snowplowanalytics.storage.googlecloudstorage.loader.Options
```

### Additional information

A full list of all the Beam CLI options can be found at:
https://cloud.google.com/dataflow/pipelines/specifying-exec-params#setting-other-cloud-pipeline-options.

## Testing

To run the tests:

```
sbt test
```

## REPL

To experiment with the current codebase in [Scio REPL](https://github.com/spotify/scio/wiki/Scio-REPL)
simply run:

```
sbt repl/run
```

## Find out more

| Technical Docs              | Setup Guide           |
|-----------------------------|-----------------------|
| ![i1][techdocs-image]       | ![i2][setup-image]    |
| [Technical Docs][techdocs]  | [Setup Guide][setup]  |

## Copyright and license

Copyright 2018-2019 Snowplow Analytics Ltd.

Licensed under the [Apache License, Version 2.0][license] (the "License");
you may not use this software except in compliance with the License.

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

[pubsub]: https://cloud.google.com/pubsub/
[storage]: https://cloud.google.com/storage/
[dataflow]: https://cloud.google.com/dataflow/
[templates]: https://cloud.google.com/dataflow/docs/templates/overview
[executing-templates]: https://cloud.google.com/dataflow/docs/templates/executing-templates

[bintray]: https://bintray.com/snowplow/snowplow-generic/snowplow-google-cloud-storage-loader
[bintray-docker]: https://bintray.com/snowplow/registry/snowplow%3Asnowplow-google-cloud-storage-loader

[license]: http://www.apache.org/licenses/LICENSE-2.0

[techdocs-image]: https://d3i6fms1cm1j0i.cloudfront.net/github/images/techdocs.png
[setup-image]: https://d3i6fms1cm1j0i.cloudfront.net/github/images/setup.png
[techdocs]: https://github.com/snowplow/snowplow/wiki/Snowplow-Google-Cloud-Storage-Loader
[setup]: https://github.com/snowplow/snowplow/wiki/setting-up-snowplow-google-cloud-storage-loader
