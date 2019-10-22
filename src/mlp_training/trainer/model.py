
from google.cloud import storage
import data


def process_data():
    training_df = data.get_data_partition("train")
    print(training_df)

# def recall_metric(y_true, y_pred):
#     """
#     TODO: description
#     :param y_true:
#     :param y_pred:
#     :return:
#     """

#     true_positives = K.sum(K.round(K.clip(y_true * y_pred, 0, 1)))
#     possible_positives = K.sum(K.round(K.clip(y_true, 0, 1)))
#     recall = true_positives / (possible_positives + K.epsilon())

#     return recall


# def precision_metric(y_true, y_pred):
#     """
#     TODO: description
#     :param y_true:
#     :param y_pred:
#     :return:
#     """

#     true_positives = K.sum(K.round(K.clip(y_true * y_pred, 0, 1)))
#     predicted_positives = K.sum(K.round(K.clip(y_pred, 0, 1)))
#     precision = true_positives / (predicted_positives + K.epsilon())

#     return precision


# def upload_blob(bucket_name, source_file_name, destination_blob_name):
#     """
#     Uploads a file to the bucket
#     :param bucket_name:
#     :param source_file_name:
#     :param destination_blob_name:
#     :return:
#     """

#     storage_client = storage.Client()
#     bucket = storage_client.get_bucket(bucket_name)
#     blob = bucket.blob(destination_blob_name)
#     blob.upload_from_filename(source_file_name)

# def save_model(mlp_model, history, bucket, job_dir):
#     """
#     TODO: description
#     :param mlp_model:
#     :param history:
#     :param job_dir:
#     :return:
#     """