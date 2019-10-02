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