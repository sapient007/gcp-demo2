import argparse
import trainer.model as model
import numpy as np
import xgboost as xgb


def train_and_evaluate(args: dict):
    """
    TODO: description
    :param args:
    :return:
    """
    x_train, y_train, x_test, y_test, cols = model.process_data()
    xg_reg = model.train(x_train, y_train, x_test, y_test, cols, args)
    model.save_model(xg_reg, args.get("bucket"), args.get("bucket_path"), args.get("filename"))
    model.delete_model(args.get("filename"))
    evaluate(xg_reg, x_train, y_train, x_test, y_test, args)


def evaluate(xg_reg: xgb.XGBRegressor, x_train: np.array, y_train: np.array, x_test: np.array, y_test: np.array, args: dict):
    r2 = model.r2(xg_reg, x_test, y_test)
    print("R^2: %.2f" % (r2))

    y_pred = model.predict_regressor(xg_reg, x_test)

    score = model.rmse(y_pred, y_test)
    print("Test RMSE: %.2f" % (score))


def get_parser() -> argparse.ArgumentParser:
    parser = argparse.ArgumentParser()
    parser.add_argument("bucket", type=str)
    parser.add_argument("--filename", type=str, default="model.pkl")
    parser.add_argument("--bucket_path", type=str, default="model")
    parser.add_argument("--eta", type=float, default=0.1)
    parser.add_argument("--max_depth", type=int, default=3)
    parser.add_argument("--subsample", type=float, default=1.0)
    parser.add_argument("--lambda", type=float, default=1.0)
    parser.add_argument("--alpha", type=float, default=0)
    parser.add_argument("--tree_method", type=str, default="hist")
    parser.add_argument("--predictor", type=str, default="cpu_predictor")
    parser.add_argument("--n_jobs", type=int, default=1)
    parser.add_argument("--objective", type=str, default="reg:linear")
    parser.add_argument("--eval_metric ", type=str, default="rmse")


if __name__ == '__main__':
    parser = get_parser()
    args, _ = parser.parse_known_args()
    train_and_evaluate(vars(args))
