import datetime
import xgboost as xgb
import pandas as pd
from sklearn.preprocessing import LabelEncoder
from sklearn.preprocessing import OneHotEncoder
import subprocess
from google.cloud import storage
import pandas as pd
import numpy as np

from sklearn import preprocessing
from math import sqrt
import datetime
#import matplotlib.pyplot as plt
#import seaborn as sns
from xgboost.sklearn import XGBRegressor
from sklearn.model_selection import cross_validate
from sklearn.model_selection import learning_curve

# Fill in your Cloud Storage bucket name
BUCKET_ID = 'friday_demo2'

public_bucket = storage.Client().bucket(BUCKET_ID)
blob = public_bucket.blob('Data/train.csv')
blob.download_to_filename('train.csv')

blob = public_bucket.blob('Data/test.csv')
blob.download_to_filename('test.csv')

#Read the data from the bucket
train = pd.read_csv('train.csv')
test = pd.read_csv('test.csv')


numeric_features = train.select_dtypes(include=[np.number])
numeric_features.dtypes


# categorical columns to convert
categorical_columns = ["Gender", "Age", "Occupation", "City_Category", "Stay_In_Current_City_Years",
                       "Marital_Status", "Product_Category_1"]


# Join Train and Test Dataset so it can be cleaned all at once
train['source']='train'
test['source']='test'

data = pd.concat([train,test], ignore_index = True, sort = False)

#Get index of all columns with product_category_1 equal 19 or 20 from train and remove since not populated
condition = data.index[(data.Product_Category_1.isin([19,20])) & (data.source == "train")]
data = data.drop(condition)

# define example
#community_area = [num for num in range(78)]
# data = array(data)
#print(community_area)
# one hot encode

# convert categorical data to to numerical values.
# convert data in categorical columns to numerical values
"""encoders = {col:LabelEncoder() for col in categorical_columns}
for col in categorical_columns:
    data[col] = encoders[col].fit_transform(data[col])
    #data[col] = to_categorical(data[col])
    #print(data[col])
"""
data = pd.get_dummies(data, columns=categorical_columns, drop_first=False)
#print(data)

totalitem = data['User_ID'].value_counts().sort_index()
totalpurchase = data.groupby('User_ID').sum()['Purchase']
tot = pd.concat([totalitem, totalpurchase], axis =1, keys = ['Total_products', 'Total_purchase'])
data = pd.merge(data, tot, left_on = 'User_ID', right_index = True)
    
#Divide into test and train
train = data.loc[data['source']=="train"]
test = data.loc[data['source']=="test"]

#Drop unnecessary columns:
test.drop(['source'],axis=1,inplace=True)
train.drop(['source'],axis=1,inplace=True)

# remove column we are trying to predict ('Purchase') from features list and also removed product category 2 and 3 due to missing values
train_features = train.drop(['Purchase', 'Product_Category_2', 'Product_Category_3', 'Product_ID', 'User_ID'], axis=1)
test_features = test.drop(['Purchase', 'Product_Category_2', 'Product_Category_3', 'Product_ID', 'User_ID'], axis=1)
# create training labels list
train_labels = train[['Purchase']]
test_labels = test[['Purchase']]

# load data into DMatrix object
dtrain = xgb.DMatrix(train_features, train_labels)
dtest = xgb.DMatrix(test_features)
# train model
bst = xgb.train({}, dtrain, 20)


# Export the model to a file
model = 'model.bst'
bst.save_model('./model.bst')

# Upload the model to Cloud Storage
bucket = storage.Client().bucket(BUCKET_ID)
blob = bucket.blob(model)
blob.upload_from_filename(model)


#tree based learner for onehotencode
from sklearn.metrics import mean_squared_error
xg_reg = xgb.XGBRegressor(colsample_bytree = 0.3, learning_rate = 0.1,
                max_depth = 5, alpha = 10, n_estimators = 200)

xg_reg.fit(train_features,train_labels)

preds = xg_reg.predict(train_features)
rmse = np.sqrt(mean_squared_error(train_labels, preds))
print("RMSE: %f" % (rmse))
