from google.cloud import storage
from typing import Dict, Tuple, Sequence, List
import os
import xgboost as xgb
from sklearn.metrics import accuracy_score
from sklearn.metrics import explained_variance_score
import data
import numpy as np


def process_data() -> Tuple[np.array, np.array, np.array, np.array, List[str]]:
    train_raw = data.get_data_partition("train")
    test_raw = data.get_data_partition("test")

    x_train = train_raw.drop(['Purchase_Total', ], axis=1)
    y_train = train_raw['Purchase_Total']
    x_test = test_raw.drop(['Purchase_Total'], axis=1)
    y_test = test_raw['Purchase_Total']

    return x_train, y_train, x_test, y_test, train_raw.drop(['Purchase_Total', ], axis=1).columns


def train(x_train: np.array, y_train: np.array, x_test: np.array, y_test: np.array, cols, params) -> Tuple[xgb.Booster, dict]:
    dtrain = xgb.DMatrix(x_train, label=y_train, feature_names=cols)
    dtest = xgb.DMatrix(x_test, y_test)
    evals_result = {}

    bst = xgb.train({}, dtrain, 20, [(dtrain, "dtrain"),
            (dtest, "dtest")], evals_result=evals_result)
    return bst, evals_result


def fit_regressor(file: str, x_train: np.array, y_train: np.array, x_test: np.array, y_test: np.array, n_jobs=4):
    model = xgb.XGBRegressor(n_jobs=n_jobs)
    model.load_model(file)
    model.fit(x_train, y_train)
    return model


def predict(bst: xgb.Booster, x_test: np.array) -> np.array:
    dtest = xgb.DMatrix(x_test)
    return bst.predict(dtest, ntree_limit=20)


def accuracy(model: xgb.XGBRegressor, x_val, y_val) -> float:
    y_pred = model.predict(x_test)
    predictions = [round(value) for value in y_pred]
    accuracy = accuracy_score(y_test, predictions)
    return accuracy * 100.0


def variance_score(model: xgb.XGBRegressor, x_test: np.array, y_test: np.array) -> float:
    predictions = model.predict(x_test)
    return explained_variance_score(y_test, y_pred)


def r2(model: xgb.XGBRegressor, x_test: np.array, y_test: np.array) -> float:
    return model.score(x_test, y_test)


# https://machinelearningmastery.com/evaluate-gradient-boosting-models-xgboost-python/
# def kfold_accuracy(bst: xgb.Booster, x_test: np.array, y_test: np.array, n_jobs=4) -> Tuple[float]:
#     model = xgb.XGBClassifier(n_jobs=n_jobs)
#     kfold = KFold(n_splits=10, random_state=7)
#     results = cross_val_score(model, x_test, y_test, cv=kfold)
#     return results.mean() * 100, results.std() * 100


def recall_metric(y_true, y_pred) -> int:
    """
    TODO: description
    :param y_true:
    :param y_pred:
    :return:
    """

    true_positives = K.sum(K.round(K.clip(y_true * y_pred, 0, 1)))
    possible_positives = K.sum(K.round(K.clip(y_true, 0, 1)))
    recall = true_positives / (possible_positives + K.epsilon())

    return recall


def precision_metric(y_true, y_pred) -> int:
    """
    TODO: description
    :param y_true:
    :param y_pred:
    :return:
    """

    true_positives = K.sum(K.round(K.clip(y_true * y_pred, 0, 1)))
    predicted_positives = K.sum(K.round(K.clip(y_pred, 0, 1)))
    precision = true_positives / (predicted_positives + K.epsilon())

    return precision


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


def delete_model(filename):
    os.remove(filename)
