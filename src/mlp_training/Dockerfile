FROM python:3.5-stretch

WORKDIR /opt/gcp-demo2/mlp-training
# Do this first to cache this step
COPY src/mlp_training/requirements.txt /opt/gcp-demo2/mlp-training/requirements.txt
RUN pip install -r requirements.txt
COPY src/mlp_training /opt/gcp-demo2/mlp-training

ENTRYPOINT ["python", "trainer/task.py"]