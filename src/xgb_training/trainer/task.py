import argparse
import model
import matplotlib.pyplot as plt
import xgboost as xgb
import numpy as np


filename = "model.bst"


def train_and_evaluate(args):
    """
    TODO: description
    :param args:
    :return:
    """
    x_train, y_train, x_val, y_val, cols = model.process_data()
    bst, _ = model.train(x_train, y_train, x_val, y_val, cols, args)
    model.save_model(bst, "gcp-cert-demo-2", "model", filename)
    evaluate(filename, x_train, y_train, x_val, y_val, args)


def evaluate(filename: str, x_train: np.array, y_train: np.array, x_val: np.array, y_val: np.array, args):
    xg_reg = model.fit_regressor(filename, x_train, y_train, x_val, y_val)
    model.delete_model(filename)

    r2 = model.r2(xg_reg, x_val, y_val)
    print("R^2: %.2f%%" % (r2))

    score = model.variance_score(xg_reg, x_val, y_val)
    print("Explained variance regression score: %.2f%%" % (score))

    xgb.plot_importance(xg_reg)
    plt.rcParams['figure.figsize'] = [5, 5]
    plt.show()


if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    args, _ = parser.parse_known_args()
    train_and_evaluate(args)
