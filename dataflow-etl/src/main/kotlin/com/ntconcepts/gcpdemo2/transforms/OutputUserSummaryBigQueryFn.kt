package com.ntconcepts.gcpdemo2.transforms

import com.google.api.services.bigquery.model.TableRow
import com.ntconcepts.gcpdemo2.models.UserSummary
import org.apache.beam.sdk.transforms.SimpleFunction

class OutputUserSummaryBigQueryFn : SimpleFunction<UserSummary, TableRow>() {
    override fun apply(obj: UserSummary): TableRow {
        return UserSummary.toTableRow(obj)
    }
}