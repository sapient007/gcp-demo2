FROM python:3.7-stretch

WORKDIR /opt/gcp-demo2/mlp-training
COPY src/mlp_training /opt/gcp-demo2/mlp-training
RUN pip install -r requirements.txt
ENTRYPOINT ["python", "trainer/task.py"]