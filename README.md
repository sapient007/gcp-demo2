# Demo 2 - GCP ML Specialization Certification

This code package leverages GCP tools to create a model capable of
predicting the amount of money a shopper will spend on Black Friday.

## Setup

### Requirements
Python 3.5  
Package Manager - Anaconda3
### Install Anaconda
[Anaconda Distribution](https://docs.anaconda.com/anaconda/install/)

### Setup Environment
```
conda create -n gcp-demo2 -y python=3.7.4
```

Activate the virtual environment
```
source activate gcp-demo2
```
or in some shells
```
conda activate gcp-demo2
```
You can deactivate with
```
source deactivate
```
or in some shells
```
conda deactivate
```

TODO - Package Installation
### Building Source Code
```
python setup.py bdist_wheel sdist
cd dist
pip install -U <filename.whl>
```

### Authenticating to GCP
Set GOOGLE_APPLICATION_CREDENTIALS environment variable to the path to the SA credentials provided.  

Windows -
```
set GOOGLE_APPLICATION_CREDENTIALS=/path/to/credentials.json
```

Linux -
```
export GOOGLE_APPLICATION_CREDENTIALS=/path/to/credentials.json
```

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

## Training

A subset of [hyperparameters](https://xgboost.readthedocs.io/en/latest/python/python_api.html#module-xgboost.sklearn) are available to set as Python arguments for the training job:

- `--eta`
- `--max_depth`
- `--subsample`
- `--lambda`
- `--alpha`
- `--tree_method`
- `--predictor`
- `--n_jobs`
- `--objective`
- `--eval_metric`

### Use gcloud to package and start training job

This method is the easiest. Run the command from inside `./xgb_training`.

```bash
gcloud ai-platform jobs submit training "blackfriday_"$(date +"%Y%m%d_%H%M%S") \
    --region us-east1 \
    --job-dir gs://$BUCKET_NAME/model/output \
    --staging-bucket gs://$BUCKET_NAME \
    --package-path=xgb_training/trainer \
    --module-name trainer.task \
    --runtime-version 1.14 \
    --python-version 3.5 \
    --scale-tier CUSTOM \
    --master-machine-type n1-standard-4 \
    -- $BUCKET_NAME --n_jobs=4


gcloud ai-platform jobs submit training "blackfriday_tune_"$(date +"%Y%m%d_%H%M%S") \
    --region us-east1 \
    --job-dir gs://$BUCKET_NAME/model/output \
    --staging-bucket gs://$BUCKET_NAME \
    --package-path=xgb_training/trainer \
    --module-name trainer.task \
    --runtime-version 1.14 \
    --python-version 3.5 \
    --scale-tier CUSTOM \
    --master-machine-type n1-standard-8 \
    --config $HPTUNING_CONFIG \
    -- --n_jobs=8 tune
```

### Using a container

The job can also be started from a custom-built container. Use this method if AI Platform runtime updates cause dependency problems. It requires enabling the Container Registry API.

#### Build the container with the training source code

```bash
docker build --pull -f .\src\xgb_training\Dockerfile -t gcr.io/$PROJECT_ID/gcp-demo2:training ./
```

#### Push container to GCP Container Registry

```bash
docker push gcr.io/$PROJECT_ID/gcp-demo2:training
```

### Starting the training job

Note for Windows users: Use `"blackfriday_$(Get-Date -UFormat "%Y%m%d_%H%M%S")"` for the job name.

```bash
gcloud ai-platform jobs submit training "blackfriday_tune_"$(date +"%Y%m%d_%H%M%S") \
    --region us-east1 \
    --job-dir gs://$BUCKET_NAME/model/output \
    --master-image-uri gcr.io/$PROJECT_ID/gcp-demo2:training \
    --scale-tier CUSTOM \
    --master-machine-type n1-standard-4 \
    --config $HPTUNING_CONFIG \
    -- --n_jobs=4 tune

```

## Hyperparameter tuning

### Use gcloud to package and start an AI Platform hyperparameter tuning job

Run the command from inside `./xgb_training`.

#### Linux/macOS

```bash
gcloud ai-platform jobs submit training "blackfriday_tune_"$(date +"%Y%m%d_%H%M%S") \
    --region us-east1 \
    --job-dir gs://$BUCKET_NAME/model/output \
    --staging-bucket gs://$BUCKET_NAME \
    --master-image-uri=gcr.io/$PROJECT_ID/gcp-demo2:training \
    --module-name trainer.task \
    --scale-tier CUSTOM \
    --master-machine-type n1-standard-8 \
    --config hptuning_config.yaml \
    -- --n_jobs=8 tune
    # --package-path=xgb_training/trainer \
    # --runtime-version 1.14 \
    # --python-version 3.5 \
```

#### Windows

```powershell
gcloud ai-platform jobs submit training "blackfriday_tune_"$(date +"%Y%m%d_%H%M%S") \
    --region us-east1 \
    --job-dir gs://$env:BUCKET_NAME/model/output \
    --staging-bucket gs://$env:BUCKET_NAME \
    --package-path=xgb_training/trainer \
    --module-name trainer.task \
    --runtime-version 1.14 \
    --python-version 3.5 \
    --scale-tier CUSTOM \
    --master-machine-type n1-standard-8 \
    --config hptuning_config.yaml \
    -- --n_jobs=8 tune
```

## Deployment

### Creating the deployment 

Whichever method chosen to train the model, a pickled version of the trained model is saved into GCS that can be used to create an online prediction deployment.

```bash
gcloud ai-platform versions create $VERSION_NAME \
  --model $MODEL_NAME \
  --origin $MODEL_DIR \
  --runtime-version=1.14 \
  --framework SCIKIT_LEARN \
  --python-version=3.5
```

When using the default options, the `$MODEL_DIR` will be: `gs://$BUCKET_NAME/model`

Set the new version to be the default

```bash
gcloud ai-platform versions set-default $VERSION_NAME --model=$MODEL_NAME
```