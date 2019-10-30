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

```bash
docker build -f .\src\xgb_training\Dockerfile -t gcr.io/ml-sandbox-1-191918/gcp-demo2:training ./
```

```bash
docker push gcr.io/ml-sandbox-1-191918/gcp-demo2:training
```

### Starting the training job

```bash
gcloud ai-platform jobs submit training "blackfriday_"$(date +"%Y%m%d_%H%M%S") \
    --region us-east1 \
    --job-dir gs://gcp-cert-demo-2/model/output \
    --master-image-uri gcr.io/ml-sandbox-1-191918/gcp-demo2:training
```

## Deployment

### Creating the deployment 

```bash
gcloud ml-engine versions create $VERSION_NAME \
    --model=$MODEL_NAME \
    --framework=xgboost \
    --origin=$BOOSTER_PATH \
    --python-version=3.5 \
    --runtime-version=1.14
```

Set the new version to be the default

```bash
gcloud ml-engine versions set-default $VERSION_NAME \
    --model=$MODEL_NAME 
```