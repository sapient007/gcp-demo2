import argparse
import model

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

    accuracy = model.accuracy(filename, x_train, y_train, y_val, x_val)
    print("Accuracy: %.2f%%" % (accuracy))

    model.delete_model(filename)

    # acc, std = model.kfold_accuracy(bst, x_val, y_val)
    # print("Accuracy: %.2f%% (%.2f%%)" % (acc, std))

    # y_preds = model.predict(bst, x_val)
    # print(model.precision_metric(y_val, y_preds))


if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    args, _ = parser.parse_known_args()
    train_and_evaluate(args)
