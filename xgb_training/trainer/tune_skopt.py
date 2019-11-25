from functools import partial
from skopt import gp_minimize
from skopt.space import Real, Integer, Categorical
import trainer.model as model
import trainer.task as task
import copy

space = [
    Real(.0001, 1, name="eta"),
    Integer(2, 50, name="max_depth"),
    Real(0.5, 1, name="subsample"),
    Real(.0001, 10, name="lambda", prior="log-uniform"),
    Real(.0001, 10, name="alpha", prior="log-uniform"),
    # Categorical(["cpu_predictor"], None, None, "predictor"),
    # Categorical(["hist", "exact", "approx"], None, None, "tree_method"),
    # Categorical(["reg:linear", "count:poisson"], None, None, "objective"), validation rmse
    # Categorical(["rmse"], None, None, "eval_metric"), 
]

models = []
model_scores = []
model_rmse = []

parser = task.get_parser()
args, _ = parser.parse_known_args()
default_params = vars(args)

def return_model_assessment(args, x_train, y_train, x_test):
    global models, train_scores, test_scores, curr_model_hyper_params
    params = {curr_model_hyper_params[i]: args[i] for i, j in enumerate(curr_model_hyper_params)}
    
    default_copy = copy.deepcopy(default_params)
    default_copy.update(params)
    
    xg_reg = model.train(x_train, y_train, default_copy)
    models.append(xg_reg)

    r = model.r2(xg_reg, x_test, y_test)
    model_scores.append(r)
    y_pred = model.predict_regressor(xg_reg, x_test)
    rmse = model.rmse(y_pred, y_test)
    model_rmse.append(rmse)
    # optimize for r^2
    return rmse

x_train, y_train, x_test, y_test, _ = model.process_data()

curr_model_hyper_params = ['eta', 
                           'max_depth',
                           'subsample',
                           'lambda',
                           'alpha',
                        #    'predictor',
                        #    'tree_method',
                        #    'objective',
                        #    'eval_metric'
                           ]
objective_function = partial(return_model_assessment, x_train=x_train, y_train=y_train, x_test=x_test)

# running the algorithm
n_calls = default_params.get("n_calls") # number of times you want to train your model
results = gp_minimize(objective_function, space, base_estimator=None, n_calls=n_calls, n_random_starts=n_calls - 1, random_state=42, n_jobs=default_params.get("n_jobs"))

print(max(model_scores))
print(min(model_rmse))
print(results.x)
print(results.fun)