package com.ntconcepts.gcpdemo2.utils

import com.google.cloud.bigquery.Field
import com.google.cloud.bigquery.StandardSQLTypeName

@Target(AnnotationTarget.PROPERTY, AnnotationTarget.PROPERTY_GETTER)
annotation class BigQueryField(val sqlType: StandardSQLTypeName, val mode: Field.Mode)