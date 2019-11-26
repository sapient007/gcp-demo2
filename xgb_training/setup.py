from setuptools import find_packages
from setuptools import setup

REQUIRED_PACKAGES = [
    'google-cloud-bigquery-storage[pandas,fastavro]',
    'google-cloud-storage==1.20.0',
    'scikit-optimize==0.5.2',
    'cloudml-hypertune==0.1.0.dev5',
    "click==7.0"
]

setup(
    name='trainer',
    version='0.1',
    author="NT Concepts",
    description="GCP Demo 2 source code package.",
    install_requires=REQUIRED_PACKAGES,
    packages=find_packages(),
    include_package_data=True,
    python_requires='>=3.5'
)
