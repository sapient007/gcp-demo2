import trainer.model as model
import hypertune

def tune(params: dict, shards: int):
    _, rsme_scores, r2_scores = model.train_shards(params, shards=shards)

    hpt = hypertune.HyperTune()
    # Report the final batch scores for RMSE and R^2 for training
    hpt.report_hyperparameter_tuning_metric(
        hyperparameter_metric_tag='rmse',
        metric_value=float(rsme_scores.pop()),
        global_step=1001)
    hpt.report_hyperparameter_tuning_metric(
        hyperparameter_metric_tag='r2',
        metric_value=float(r2_scores.pop()),
        global_step=1000)
