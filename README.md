# Demo 2 - GCP ML Specialization Certification

This code package leverages GCP tools to create a model capable of
predicting the amount of money a shopper will spend on Black Friday.

## Setup

### Requirements
Python 3.7.4  
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

## Training

### Building the container

#### Build the container with the training source code

```bash
docker build --pull -f .\src\xgb_training\Dockerfile --build-arg BUCKET=$BUCKET_NAME -t gcr.io/$PROJECT_ID/gcp-demo2:training ./
```

#### Push container to GCP Container Registry

```bash
docker push gcr.io/$PROJECT_ID/gcp-demo2:training
```

### Starting the training job

```bash
gcloud ai-platform jobs submit training "blackfriday_"$(date +"%Y%m%d_%H%M%S") \
    --region us-east1 \
    --job-dir gs://gcp-cert-demo-2/model/output \
    --staging-bucket gs://gcp-cert-demo-2 \
    --package-path=xgb_training/trainer \
    --module-name trainer.task \
    --runtime-version 1.14 \
    --python-version 3.5 \
    --scale-tier CUSTOM \
    --master-machine-type n1-standard-4 \
    -- gcp-cert-demo-2 \
    -- --n_jobs=4

gcloud ai-platform jobs submit training "blackfriday_"$(date +"%Y%m%d_%H%M%S") \
    --region us-east1 \
    --job-dir gs://gcp-cert-demo-2/model/output \
    --master-image-uri gcr.io/$PROJECT_ID/gcp-demo2:training \
    --scale-tier CUSTOM \
    --master-machine-type n1-standard-4
```

## Deployment

### Creating the deployment 

```bash
gcloud ai-platform versions create $VERSION_NAME \
  --model $MODEL_NAME \
  --origin $MODEL_DIR \
  --runtime-version=1.14 \
  --framework SCIKIT_LEARN \
  --python-version=3.5
```

Set the new version to be the default

```bash
gcloud ai-platform versions set-default $VERSION_NAME --model=$MODEL_NAME
```