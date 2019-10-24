package com.ntconcepts.gcpdemo2.dataprep.transforms

import com.google.api.services.bigquery.model.TableRow
import com.ntconcepts.gcpdemo2.dataprep.models.Purchase
import org.apache.beam.sdk.transforms.SimpleFunction

class OutputPurchaseBigQueryFn : SimpleFunction<Purchase, TableRow>() {
    override fun apply(obj: Purchase): TableRow {
        return Purchase.toTableRow(obj)
    }
}