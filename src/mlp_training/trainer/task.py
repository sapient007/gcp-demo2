import argparse
import model


def train_and_evaluate(args):
    """
    TODO: description
    :param args:
    :return:
    """
    x_train, y_train, x_val, y_val, cols = model.process_data()
    bst, _ = model.train_mlp(x_train, y_train, x_val, y_val, cols, args)
    model.save_model(bst, "gcp-cert-demo-2", "model", "model.bst")


if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    args, _ = parser.parse_known_args()
    train_and_evaluate(args)