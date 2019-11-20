FROM python:3.5-stretch

WORKDIR /opt/gcp-demo2/xgb-training
# Do this first to cache this step
COPY src/xgb_training/requirements.txt /opt/gcp-demo2/xgb-training/requirements.txt
RUN pip install -r requirements.txt
COPY src/xgb_training /opt/gcp-demo2/xgb-training

ENTRYPOINT ["python", "-m", "trainer.task"]