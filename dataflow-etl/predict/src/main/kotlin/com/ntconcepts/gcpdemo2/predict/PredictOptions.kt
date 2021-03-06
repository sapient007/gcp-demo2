package com.ntconcepts.gcpdemo2.predict

import org.apache.beam.runners.dataflow.options.DataflowPipelineOptions
import org.apache.beam.sdk.options.Default
import org.apache.beam.sdk.options.Description
import org.apache.beam.sdk.options.ValueProvider

interface PredictOptions : DataflowPipelineOptions {

    @get:Description("Bigquery black friday dataset")
    @get:Default.String("blackfriday")
    val dataset: ValueProvider<String>

    fun setDataset(dataset: ValueProvider<String>)

    @get:Description("Bigquery user_summary table")
    @get:Default.String("user_summaries")
    val table: ValueProvider<String>

    fun setTable(table: ValueProvider<String>)

    @get:Description("Bigquery UserSummary output tablespec. Example: project_id:dataset.table")
    @get:Default.String("blackfriday.user_summaries")
    val userSummaryTableSpec: ValueProvider<String>

    fun setUserSummaryTableSpec(tableSpec: ValueProvider<String>)

    @get:Description("Name of label field in the table")
    @get:Default.String("Purchase_Total")
    val labelName: ValueProvider<String>

    fun setLabelName(labelName: ValueProvider<String>)

    @get:Description("ID of deployed model on AI Platform")
    @get:Default.String("blackfriday")
    val modelId: ValueProvider<String>

    fun setModelId(modelId: ValueProvider<String>)

    @get:Description("ID of version of deployed model on AI Platform")
    val modelVersionId: ValueProvider<String>

    fun setModelVersionId(modelVersionId: ValueProvider<String>)

    @get:Description("Bigquery predicted user summaries output table")
    @get:Default.String("user_summaries_pred")
    val outputUserPredictedTable: ValueProvider<String>

    fun setOutputUserPredictedTable(table: ValueProvider<String>)

    @get:Description("Bigquery predicted user summaries output table spec")
    @get:Default.String("blackfriday.user_summaries_pred")
    val outputUserPredictedTableSpec: ValueProvider<String>

    fun setOutputUserPredictedTableSpec(table: ValueProvider<String>)

    @get:Description("Drop output table when job starts")
    @get:Default.Boolean(true)
    val dropTable: ValueProvider<Boolean>

    fun setDropTable(dropTable: ValueProvider<Boolean>)
}