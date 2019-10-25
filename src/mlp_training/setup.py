from setuptools import find_packages
from setuptools import setup


REQUIRED_PACKAGES = [
    'scikit-learn==0.20.2',
    'xgboost==0.81',
    'pandas==0.24.0',
    'google-cloud-bigquery-storage[pandas,fastavro]',
    'google-cloud-storage==1.20.0'
]


setup(
    name='trainer',
    version='0.1',
    install_requires=REQUIRED_PACKAGES,
    packages=find_packages(),
    include_package_data=True,
    description='GCP Demo 2 Training Application',
    python_requires='>=3.5'
)