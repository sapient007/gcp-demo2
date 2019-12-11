# XGBoost training package

TODO: Some pithy sentence

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

### Using a container

The job can be started from a custom-built container. It requires enabling the Container Registry API in your GCP project.

#### Build the container with the training source code and push

Run this from inside `./xgb_training`

##### Linux
```bash
docker build --pull -f ./Dockerfile -t gcr.io/$PROJECT_ID/gcp-demo2:training ./ && docker push gcr.io/$PROJECT_ID/gcp-demo2:training
```

##### Windows

```powershell
docker build --pull -f ./Dockerfile -t gcr.io/$env:PROJECT_ID/gcp-demo2:training ./; docker push gcr.io/$env:PROJECT_ID/gcp-demo2:training
```

### Starting the training job in ML Engine

Note for Windows users: Use `"blackfriday_$(Get-Date -UFormat "%Y%m%d_%H%M%S")"` for the job name.

```bash
gcloud ai-platform jobs submit training "blackfriday_tune_"$(date +"%Y%m%d_%H%M%S") \
    --region us-east1 \
    --job-dir gs://$BUCKET_NAME/model/output \
    --master-image-uri gcr.io/$PROJECT_ID/gcp-demo2:training \
    --scale-tier CUSTOM \
    --master-machine-type n1-standard-4 \
    -- --n_jobs=4 --eta=0.114 --max_depth=12 \
    --colsample_bytree=0.221 --subsample=0.75 \
    --lambda_param=2.317 train $BUCKET_NAME
```

## Hyperparameter tuning



### Use gcloud to package and start an AI Platform hyperparameter tuning job

Run the command from inside `./xgb_training`.

#### Linux/macOS

```bash
gcloud ai-platform jobs submit training "blackfriday_tune_"$(date +"%Y%m%d_%H%M%S") \
    --region us-east4 \
    --job-dir gs://$BUCKET_NAME/model/output \
    --master-image-uri gcr.io/$PROJECT_ID/gcp-demo2:training \
    --scale-tier CUSTOM \
    --master-machine-type n1-standard-4 \
    --config hptuning_config.yaml \
    -- --n_jobs=4 tune
```

#### Windows

```powershell
gcloud ai-platform jobs submit training "blackfriday_tune_"$(date +"%Y%m%d_%H%M%S") \
    --region us-east4 \
    --job-dir gs://$BUCKET_NAME/model/output \
    --master-image-uri gcr.io/$PROJECT_ID/gcp-demo2:training \
    --scale-tier CUSTOM \
    --master-machine-type n1-standard-4 \
    --config hptuning_config.yaml \
    -- --n_jobs=4 tune
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