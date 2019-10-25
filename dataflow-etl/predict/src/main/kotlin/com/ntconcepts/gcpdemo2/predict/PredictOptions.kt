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
}