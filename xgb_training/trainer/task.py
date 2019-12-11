import trainer.model as model
import trainer.tune as tuner
import click


@click.group()
# @click.option("--eta", type=float, default=0.082)
# @click.option("--max_depth", type=int, default=12)
# @click.option("--subsample", type=float, default=1.0)
# @click.option("--colsample_bytree", type=float, default=0.286)
# @click.option("--lambda_param", type=float, default=8.151)
# @click.option("--alpha", type=float, default=0)
# @click.option("--tree_method", type=str, default="hist")
# @click.option("--predictor", type=str, default="cpu_predictor")
# @click.option("--n_jobs", type=int, default=1)
# @click.option("--objective", type=str, default="reg:linear")
# @click.option("--eval_metric", type=str, default="rmse")
# @click.option("--project", type=str, default='ml-sandbox-1-191918')
@click.option("--eta", type=float, default=0.1)
@click.option("--max_depth", type=int, default=6)
@click.option("--subsample", type=float, default=1.0)
@click.option("--lambda_param", type=float, default=1.0)
@click.option("--alpha", type=float, default=0)
@click.option("--tree_method", type=str, default="hist")
@click.option("--predictor", type=str, default="cpu_predictor")
@click.option("--n_jobs", type=int, default=1)
@click.option("--objective", type=str, default="reg:linear")
@click.option("--eval_metric", type=str, default="rmse")
@click.option("--colsample_bytree", type=float, default=1.0)
@click.pass_context
def cli(ctx, eta: float, max_depth: int,
        subsample: float, lambda_param: float, alpha: float, tree_method: str,
        predictor: str, n_jobs: int, objective: str, eval_metric: str, colsample_bytree: float):
    ctx.ensure_object(dict)
    ctx.obj["params"] = {}
    ctx.obj["params"]["eta"] = eta
    ctx.obj["params"]["max_depth"] = max_depth
    ctx.obj["params"]["subsample"] = subsample
    ctx.obj["params"]["lambda"] = lambda_param
    ctx.obj["params"]["alpha"] = alpha
    ctx.obj["params"]["tree_method"] = tree_method
    ctx.obj["params"]["predictor"] = predictor
    ctx.obj["params"]["n_jobs"] = n_jobs
    ctx.obj["params"]["objective"] = objective
    ctx.obj["params"]["eval_metric"] = eval_metric
    ctx.obj["params"]["colsample_bytree"] = colsample_bytree


@click.command()
@click.pass_context
@click.argument('bucket', type=str)
@click.option('--filename', type=str, default="model.bst")
@click.option("--bucket_path", type=str, default="model")
@click.option("--shards", type=int, default=1)
@click.option("--job-dir", type=str, default="")
def train(ctx, bucket: str, bucket_path: str, filename: str, shards: int, job_dir: str):
    """
    TODO: description
    :param args:
    :return:
    """
    click.echo("Starting training")
    xg_reg, _, _ = model.train_shards(ctx.obj.get("params"), shards=shards)
    model.save_model(xg_reg, bucket, bucket_path, filename)
    model.delete_model(filename)


@click.command()
@click.pass_context
@click.option("--eta", type=float, default=0.1)
@click.option("--max_depth", type=int, default=6)
@click.option("--subsample", type=float, default=1.0)
@click.option("--lambda_param", type=float, default=1.0)
@click.option("--alpha", type=float, default=0)
@click.option("--shards", type=int, default=1)
@click.option("--job-dir", type=str, default="")
@click.option("--colsample_bytree", type=float, default=1.0)
@click.option("--tree_method", type=str, default="hist")
def tune(ctx, shards: int, eta: float, max_depth: int,
         subsample: float, lambda_param: float, alpha: float, job_dir: str, colsample_bytree: float, tree_method: str):
    ctx.obj["params"]["eta"] = eta
    ctx.obj["params"]["max_depth"] = max_depth
    ctx.obj["params"]["subsample"] = subsample
    ctx.obj["params"]["lambda"] = lambda_param
    ctx.obj["params"]["alpha"] = alpha
    ctx.obj["params"]["colsample_bytree"] = colsample_bytree
    ctx.obj["params"]["tree_method"] = tree_method

    tuner.tune(ctx.obj.get("params"), shards)


cli.add_command(train)
cli.add_command(tune)

if __name__ == '__main__':
    cli(obj={})
    # parser = get_parser()
    # args, _ = parser.parse_known_args()
    # train_and_evaluate(vars(args))
