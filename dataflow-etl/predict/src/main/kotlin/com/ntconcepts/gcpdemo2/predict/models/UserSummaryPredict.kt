package com.ntconcepts.gcpdemo2.predict.models

import com.google.api.services.bigquery.model.TableRow
import com.google.cloud.bigquery.Field
import com.google.cloud.bigquery.StandardSQLTypeName
import com.ntconcepts.gcpdemo2.dataprep.models.OutputBase
import com.ntconcepts.gcpdemo2.dataprep.models.OutputCompanionInterface
import com.ntconcepts.gcpdemo2.dataprep.models.OutputInterface
import com.ntconcepts.gcpdemo2.dataprep.utils.BigQueryField

data class UserSummaryPredict(
    @BigQueryField(StandardSQLTypeName.INT64, Field.Mode.REQUIRED)
    var User_ID: Int?,
    @BigQueryField(StandardSQLTypeName.INT64, Field.Mode.REQUIRED)
    var Purchase_Total_Predicted: Int?,
    override var Encoded: MutableMap<String, Int>
) : OutputBase() {

    constructor() : this(
        null,
        null,
        HashMap<String, Int>()
    )

    companion object : OutputCompanionInterface {
        override fun getValueAsString(name: String, obj: OutputInterface): String? =
            OutputBase.getValueAsString(name, obj)

        override fun toTableRow(obj: OutputInterface): TableRow =
            OutputBase.toTableRow(obj)
    }

    override fun <T> makeCopy(): T? {
        return this.copy() as T
    }
}