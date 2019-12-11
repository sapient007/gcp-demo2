import os
import math
from os import path
import trainer.data as data
import pickle
import numpy as np
import xgboost as xgb
# from sklearn.metrics import accuracy_score
from sklearn.metrics import explained_variance_score
from sklearn.metrics import mean_squared_error
from google.cloud import storage
from typing import Tuple, List


def process_data(test_partition_name="test") -> Tuple[np.array, np.array, np.array, np.array, List[str]]:
    train_raw = data.get_data_partition("train")
    test_raw = data.get_data_partition(test_partition_name)

    x_train = train_raw.drop(['Purchase_Total', ], axis=1)
    y_train = train_raw['Purchase_Total']
    x_test = test_raw.drop(['Purchase_Total'], axis=1)
    y_test = test_raw['Purchase_Total']

    return x_train, y_train, x_test, y_test, train_raw.drop(['Purchase_Total', ], axis=1).columns


def train_shards(params: dict, test_partition_name="test", shards=1) -> Tuple[xgb.XGBRegressor, List[float], List[float]]:
    test_raw = data.get_data_partition(test_partition_name)
    x_test = test_raw.drop(['Purchase_Total'], axis=1)
    y_test = test_raw['Purchase_Total']

    session, train_readers = data.get_data_partition_sharded("train", shards)
    xg_reg = xgb.XGBRegressor()
    shard = 1
    rmse_scores = []
    r2_scores = []
    for reader in train_readers:
        reader.rows(session)
        rows = reader.rows(session)
        train_raw = rows.to_dataframe()
        x_train = train_raw.drop(['Purchase_Total', ], axis=1)
        y_train = train_raw['Purchase_Total']
        booster = ""
        if path.exists("incr_model.bst"):
            booster = "incr_model.bst"
        xg_reg = train(x_train, y_train, params, booster=booster)
        xg_reg.save_model("incr_model.bst")

        print("Shard %d train data R^2: %.2f" % (shard, r2(xg_reg, x_train, y_train)))

        r = r2(xg_reg, x_test, y_test)
        r2_scores.append(r)
        print("Shard %d test data R^2: %.2f" % (shard, r))
 
        y_pred_train = predict_regressor(xg_reg, x_train)

        train_rmse = rmse(y_pred_train, y_train)
        rmse_scores.append(train_rmse)
        print("Shard %d training data RMSE: %.2f" % (shard, train_rmse))
        
        y_pred = predict_regressor(xg_reg, x_test)

        score = rmse(y_pred, y_test)
        rmse_scores.append(score)
        print("Shard %d test data RMSE: %.2f" % (shard, score))
        shard += 1
    os.remove("incr_model.bst")
    return xg_reg, rmse_scores, r2_scores

# def train(x_train: np.array, y_train: np.array, x_test: np.array, y_test: np.array, cols, params) -> Tuple[xgb.Booster, dict]:
#     dtrain = xgb.DMatrix(x_train, label=y_train, feature_names=cols)
#     dtest = xgb.DMatrix(x_test, y_test)
#     evals_result = {}
#     bst = xgb.train({}, dtrain, 20, [(dtrain, "dtrain"),
#             (dtest, "dtest")], evals_result=evals_result)
#     return bst, evals_result


def train(x_train: np.array, y_train: np.array, params: dict, booster="") -> xgb.XGBRegressor:
    xg_reg = xgb.XGBRegressor(
        random_state=42,
        seed=42,
        n_jobs=params.get("n_jobs"),
        max_depth=params.get("max_depth"),
        subsample=params.get("subsample"),
        colsample_bytree=params.get("colsample_bytree"),
        reg_lambda=params.get("lambda"),
        reg_alpha=params.get("alpha"),
        learning_rate=params.get("eta"),
        tree_method=params.get("tree_method"),
        predictor=params.get("predictor"),
        objective=params.get("objective"),
        eval=params.get("eval_metric")
    )
    if booster != "":
        xg_reg.load_model(booster)
    xg_reg.fit(x_train, y_train)
    return xg_reg


def predict_regressor(model: xgb.XGBRegressor, x_test: np.array) -> np.array:
    return model.predict(x_test)


# def accuracy(model: xgb.XGBRegressor, x_val, y_val) -> float:
#     y_pred = model.predict(x_test)
#     predictions = [round(value) for value in y_pred]
#     accuracy = accuracy_score(y_test, predictions)
#     return accuracy * 100.0


def variance_score(y_pred: np.array, y_test: np.array) -> float:
    return explained_variance_score(y_test, y_pred)


def rmse(y_pred: np.array, y_test: np.array) -> float:
    return math.sqrt(mean_squared_error(y_test, y_pred))


def r2(model: xgb.XGBRegressor, x_test: np.array, y_test: np.array) -> float:
    return model.score(x_test, y_test)


# https://machinelearningmastery.com/evaluate-gradient-boosting-models-xgboost-python/
# def kfold_accuracy(bst: xgb.Booster, x_test: np.array, y_test: np.array, n_jobs=4) -> Tuple[float]:
#     model = xgb.XGBClassifier(n_jobs=n_jobs)
#     kfold = KFold(n_splits=10, random_state=7)
#     results = cross_val_score(model, x_test, y_test, cv=kfold)
#     return results.mean() * 100, results.std() * 100


def upload_blob(bucket_name: str, filename: str, destination_blob_name: str):
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


def save_model(xg_reg: xgb.XGBRegressor, bucket_name: str, path: str, filename: str):
    """
    TODO: description
    :param bst_model:
    :param bucket_name:
    :param path:
    :param filename:
    :return:
    """

    # with open(filename, 'w+b') as model_file:
    #     pickle.dump(xg_reg, model_file)
        
    xg_reg.save_model(filename)
    upload_blob(bucket_name, filename, "{}/{}".format(path, filename))


def delete_model(filename: str):
    os.remove(filename)
