import argparse
import model

def train_and_evaluate(args):
    """
    TODO: description
    :param args:
    :return:
    """
    model.process_data()

if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    args, _ = parser.parse_known_args()
    train_and_evaluate(args)