# Demo 2 - GCP ML Specialization Certification

This code package leverages GCP tools to create a model capable of predicting the amount of money a shopper will spend on Black Friday.

## Pipeline components

### ETL

### Model training

#### Hypertuning

## Requirements

You must have the [Google Cloud SDK](https://cloud.google.com/sdk/docs/quickstarts) installed and have a GCP project you can create resources in

### Authenticating to GCP

It is recomended to use the Google Cloud SDK (`gcloud auth application-default login`) to authenticate to GCP and run the data pipeline and create training jobs. Alternatively, you can create a GCP service account and use it with the instructions below.


Set `GOOGLE_APPLICATION_CREDENTIALS` environment variable to the path to the SA credentials provided.  

Windows (Powershell)
```powershell
$env:GOOGLE_APPLICATION_CREDENTIALS="/path/to/credentials.json"
```

Linux -
```bash
export GOOGLE_APPLICATION_CREDENTIALS=/path/to/credentials.json
```

## Local development setup instructions

### Requirements
Python 3.5  
[Anaconda](https://docs.anaconda.com/anaconda/install/)
[Google Cloud SDK](https://cloud.google.com/sdk/docs/quickstarts)

### Setup Environment
```
conda create -n gcp-demo2 -y python=3.5.5
```

Python 3.5 is recomended if training will be done with a Python package on GCP AI Platform. If using a custom image, Python 3.7 has been tested and should work.

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

### Install dependencies

```
pip install -r xgb_training/requirements.txt
```

