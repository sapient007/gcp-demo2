package com.ntconcepts.gcpdemo2.predict.transforms

import com.google.api.services.bigquery.model.TableRow
import com.ntconcepts.gcpdemo2.predict.models.UserSummaryPredict
import org.apache.beam.sdk.transforms.DoFn

class MapToTableRowsFn : DoFn<UserSummaryPredict, TableRow>() {

    @ProcessElement
    fun apply(c: ProcessContext) {
        val pred = c.element()

        val row = TableRow()
        row.set(pred::User_ID.name, pred.User_ID)
        row.set(pred::Purchase_Total_Predicted.name, pred.Purchase_Total_Predicted)

        c.output(row)

    }
}