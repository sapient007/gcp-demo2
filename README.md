# Demo 2 - GCP ML Specialization Certification

This code package leverages GCP tools to create a model capable of predicting the amount of money a shopper will spend on Black Friday.

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

#### Preprocessing and Data Pipeline ####
ETL is done by Google Cloud Dataflow job in ./dataflow-etl. The dataprep job will read CSV data stored in Google Cloud Storage and write both event-level data and user summaries into BigQuery. The predict job will read in data from BigQuery, predict labels and write User_ID and predicted purchase totals to a new BigQuery table.

For more information read: dataflow-etl/README.md

#### Model Training ####
The Scikit-Learn wrapper interface for XGBoost regression (XGBRegressor) to train our model. Using our preprocessed data, we downloaded our data from the BigQuery Storage API using multiple readers to control the amount of data being read into memory and trained. This allowed us to implement an iterative training process where we fit a shard of training data, save the model’s booster, then continue to the next shard where we load the previous model’s booster and fit the next shard to the model. We iterate through all BigQuery read session streams until we’ve trained on all data. Training used hyperparameters tuned using ML Engine’s training hyperparameter tuning feature. Parameters are discussed in more detail in the next section. Once all training data was fitted, we pickled the model and saved it to Google Cloud Storage for deployment in AI Platform. Both the training and tuning jobs were run on AI Platform using a custom container.

For more information read: xgb_training/README.md

#### Hypertuning ####
