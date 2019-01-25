GIT_COMMIT?=latest
APP_ENV?=qa
APP_NAME:=snowplow-google-cloud-storage-loader
TEMPLATE_PATH:=gs://goeuro-dags/$(APP_ENV)/dataflow_templates/$(APP_NAME)

test:
	sbt test

deploy:
	(export GOOGLE_APPLICATION_CREDENTIALS=$(shell pwd)/credentials/bi_gcloud_service_account_dev.json && sbt "runMain com.snowplowanalytics.storage.googlecloudstorage.loader.CloudStorageLoader \
		--project=goeuro-dev \
		--templateLocation=$(TEMPLATE_PATH)/SnowplowGoogleCloudStorageLoaderTemplate \
		--stagingLocation=$(TEMPLATE_PATH)/staging \
		--runner=DataflowRunner \
		--tempLocation=$(TEMPLATE_PATH)/tmp")

ci_pr: test

ci_master: test deploy

