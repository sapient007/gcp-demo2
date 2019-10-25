from google.cloud import storage
import os
import xgboost as xgb
import data


def process_data():
    train_raw = data.get_data_partition("train")
    test_raw = data.get_data_partition("test")

    x_train = train_raw.drop(['Purchase_Total', ], axis=1)
    y_train = train_raw['Purchase_Total']
    x_val = test_raw.drop(['Purchase_Total'], axis=1)
    y_val = test_raw['Purchase_Total']

    return x_train, y_train, x_val, y_val


def train_mlp(x_train, y_train, x_val, y_val, params):
    dtrain = xgb.DMatrix(x_train, y_train)
    dtest = xgb.DMatrix(x_val, y_val)
    bst = xgb.train({}, dtrain, 20, [(dtest, "test")])
    return bst

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


def upload_blob(bucket_name, filename, destination_blob_name):
    """
    Uploads a file to the bucket
    :param bucket_name:
    :param filename:
    :param destination_blob_name:
    :return:
    """

    storage_client = storage.Client()
    bucket = storage_client.get_bucket(bucket_name)
    blob = bucket.blob(destination_blob_name)
    blob.upload_from_filename(filename)


def save_model(bst_model, bucket_name, path, filename):
    """
    TODO: description
    :param bst_model:
    :param bucket_name:
    :param path:
    :param filename:
    :return:
    """

    bst_model.save_model(filename)
    upload_blob(bucket_name, filename, "{}/{}".format(path, filename))
    os.remove(filename)


