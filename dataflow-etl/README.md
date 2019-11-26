## Data pipeline

ETL is done by Google Cloud Dataflow jobs in `./dataflow-etl`. The `dataprep` job will read CSV data stored in Google Cloud Storage and write both event-level data and user summaries into BigQuery. The `predict` job will read in data from BigQuery, predict labels and write User_ID and predicted purchase totals to a new BigQuery table.

### ETL prerequisites

You will need to create a Bigquery dataset in your project. If you use the default job options, create a datased named `blackfriday`. It can be named anything and customized in job options.

Also, the data in `./data/data.csv` must be stored in a GCS bucket which the Dataflow job has access.

#### Using the shuffle service

If you want to use the Dataflow shuffle service (`--experiments=shuffle_mode=service`), you will need to run your job in a GCP region that supports the service: https://cloud.google.com/dataflow/docs/guides/deploying-a-pipeline#cloud-dataflow-shuffle

### Data prep job

#### Dataflow options

In addition to the [Cloud Dataflow Runner options](https://beam.apache.org/documentation/runners/dataflow/#pipeline-options), these options can be customized:

```
--dropTable=<ValueProvider>
  Default: true
  Drop output table when job starts
--mlPartitionTestWeight=<ValueProvider>
  Default: 15.0
  Weight to apply to random partitioning of testing data. Example: 15 for 15
  percent. Default: 15.0
--mlPartitionTrainWeight=<ValueProvider>
  Default: 70.0
  Weight to apply to random partitioning of training data. Example: 70 for 70
  percent. Default: 70.0
--mlPartitionValidationWeight=<ValueProvider>
  Default: 15.0
  Weight to apply to random partitioning of validation data. Example: 15 for
  15 percent. Default: 15.0
--outputDataset=<ValueProvider>
  Default: blackfriday
  Bigquery output dataset
--outputPurchaseTable=<ValueProvider>
  Default: purchases
  Bigquery purchases output table
--outputPurchaseTableSpec=<ValueProvider>
  Default: blackfriday.purchases
  Bigquery purchases output tablespec. Example: project_id:dataset.table
--outputUserSummaryTable=<ValueProvider>
  Default: user_summaries
  Bigquery user summary output table
--outputUserSummaryTableSpec=<ValueProvider>
  Default: blackfriday.user_summaries
  Bigquery user summary output tablespec. Example: project_id:dataset.table
--trainDataSource=<ValueProvider>
```

#### Creating a container via Docker and start job in container (recommended)

##### Step 1: Create the container

Make sure the GCP Container Registry API is enabled first: https://cloud.google.com/container-registry/docs/quickstart

###### Using application default credentials

**Windows**

```powershell
docker run --rm -v "$Env:UserProfile\AppData\Roaming\gcloud:/root/.config/gcloud"  -v '.\dataflow-etl\:/opt/etl' -w /opt/etl openjdk:8 ./gradlew :dataprep:jib --image gcr.io/$env:PROJECT_ID/$env:REPO_NAME:dataprep"
```

**macOS and Linux**

```bash
docker run --rm -v "~/.config/gcloud:/root/.config/gcloud"  -v './dataflow-etl\:/opt/etl' -w /opt/etl openjdk:8 ./gradlew :dataprep:jib --image gcr.io/$PROJECT_ID/$REPO_NAME:dataprep"
```

###### Using a service account

```bash
docker run --rm -v "$LOCATION_OF_SA_JSON:/opt/sa/key.json"  -v './dataflow-etl\:/opt/etl' -e GOOGLE_APPLICATION_CREDENTIALS=/opt/sa/key.json -w /opt/etl openjdk:8 ./gradlew :dataprep:jib --image gcr.io/$PROJECT_ID/$REPO_NAME:dataprep
```

##### Step 2: Start the job from the container

###### Using application default credentials

**Windows**

```powershell
docker run --rm -v "$env:UserProfile\AppData\Roaming\gcloud:/root/.config/gcloud" gcr.io/$env:PROJECT_ID/$env:REPO_NAME:dataprep --project=$env:PROJECT_ID --runner=DataflowRunner --region=$env:GCP_REGION --workerMachineType=$env:INSTANCE_TYPE --maxNumWorkers=$env:MAX_WORKERS --jobName=$env:JOB_NAME --tempLocation=$env:STAGING_LOCATION --trainDataSource=$env:DATA_LOCATION
```

**macOS and Linux**

```bash
docker run --rm -v "~/.config/gcloud:/root/.config/gcloud" gcr.io/$PROJECT_ID/$REPO_NAME:dataprep --project=$PROJECT_ID --runner=DataflowRunner --region=$GCP_REGION --workerMachineType=$INSTANCE_TYPE --maxNumWorkers=$MAX_WORKERS --jobName=$JOB_NAME --tempLocation=$STAGING_LOCATION --trainDataSource=$DATA_LOCATION
```

###### Using a service account

```bash
docker run --rm -v "$LOCATION_OF_SA_JSON:/opt/sa/key.json" -e GOOGLE_APPLICATION_CREDENTIALS=/opt/sa/key.json gcr.io/$PROJECT_ID/$REPO_NAME:dataprep --project=$PROJECT_ID --runner=DataflowRunner --region=$GCP_REGION --workerMachineType=$INSTANCE_TYPE --maxNumWorkers=$MAX_WORKERS --jobName=$JOB_NAME --tempLocation=$STAGING_LOCATION --trainDataSource=$DATA_LOCATION
```

### Prediction job

The model must be trained and deployed to AI Platform to run this job. See steps below to train and deploy the model.

#### Dataflow options

In addition to the [Cloud Dataflow Runner options](https://beam.apache.org/documentation/runners/dataflow/#pipeline-options), these options can be customized:

```
--dataset=<ValueProvider>
  Default: blackfriday
  Bigquery black friday dataset
--dropTable=<ValueProvider>
  Default: true
  Drop output table when job starts
--labelName=<ValueProvider>
  Default: Purchase_Total
  Name of label field in the table
--modelId=<ValueProvider>
  Default: blackfriday
  ID of deployed model on AI Platform
--modelVersionId=<ValueProvider>
  ID of version of deployed model on AI Platform
--outputUserPredictedTable=<ValueProvider>
  Default: user_summaries_pred
  Bigquery predicted user summaries output table
--outputUserPredictedTableSpec=<ValueProvider>
  Default: blackfriday.user_summaries_pred
  Bigquery predicted user summaries output table spec
--table=<ValueProvider>
  Default: user_summaries
  Bigquery user_summary table
--userSummaryTableSpec=<ValueProvider>
  Default: blackfriday.user_summaries
  Bigquery UserSummary output tablespec. Example: project_id:dataset.table
```

#### Creating a container via Docker and start job in container (recommended)

##### Step 1: Create the container

Make sure the GCP Container Registry API is enabled first: https://cloud.google.com/container-registry/docs/quickstart

###### Using application default credentials

**Windows**

```powershell
docker run --rm -v "$env:UserProfile\AppData\Roaming\gcloud:/root/.config/gcloud"  -v "$PWD/dataflow-etl/:/opt/etl" -w /opt/etl openjdk:8 ./gradlew :predict:jib --image "gcr.io/$env:PROJECT_ID/${env:REPO_NAME}:predict"
```

**macOS and Linux**

```bash
docker run --rm -v "~/.config/gcloud:/root/.config/gcloud"  -v './dataflow-etl\:/opt/etl' -w /opt/etl openjdk:8 ./gradlew :predict:jib --image gcr.io/$PROJECT_ID/$REPO_NAME:predict"
```

###### Using a service account

```bash
docker run --rm -v "$LOCATION_OF_SA_JSON:/opt/sa/key.json"  -v './dataflow-etl\:/opt/etl' -e GOOGLE_APPLICATION_CREDENTIALS=/opt/sa/key.json -w /opt/etl openjdk:8 ./gradlew :predict:jib --image gcr.io/$PROJECT_ID/$REPO_NAME:predict"
```

##### Step 2: Start the job from the container

###### Using application default credentials

**Windows**

```powershell
docker run --rm -v "$env:UserProfile\AppData\Roaming\gcloud:/root/.config/gcloud" gcr.io/$env:PROJECT_ID/$env:REPO_NAME:predict --project=$env:PROJECT_ID --runner=DataflowRunner --region=$env:GCP_REGION --workerMachineType=$env:INSTANCE_TYPE --maxNumWorkers=$env:MAX_WORKERS --jobName=$env:JOB_NAME --tempLocation=$env:STAGING_LOCATION --modelVersionId=$env:MODEL_VERSION
```

**macOS and Linux**

```bash
docker run --rm -v "~/.config/gcloud:/root/.config/gcloud" gcr.io/$PROJECT_ID/$REPO_NAME:predict --project=$PROJECT_ID --runner=DataflowRunner --region=$GCP_REGION --workerMachineType=$INSTANCE_TYPE --maxNumWorkers=$MAX_WORKERS --jobName=$JOB_NAME --tempLocation=$STAGING_LOCATION --modelVersionId=$MODEL_VERSION
```

###### Using a service account

```bash
docker run --rm -v "$LOCATION_OF_SA_JSON:/opt/sa/key.json" -e GOOGLE_APPLICATION_CREDENTIALS=/opt/sa/key.json gcr.io/$PROJECT_ID/$REPO_NAME:predict --project=$PROJECT_ID --runner=DataflowRunner --region=$GCP_REGION --workerMachineType=$INSTANCE_TYPE --maxNumWorkers=$MAX_WORKERS --jobName=$JOB_NAME --tempLocation=$STAGING_LOCATION --modelVersionId=$MODEL_VERSION
```

