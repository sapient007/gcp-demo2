package com.ntconcepts.gcpdemo2.models

import com.google.api.services.bigquery.model.TableRow
import com.google.cloud.bigquery.Field
import com.google.cloud.bigquery.StandardSQLTypeName
import com.ntconcepts.gcpdemo2.utils.BigQueryField
import java.io.Serializable

data class UserSummary(
    @BigQueryField(StandardSQLTypeName.INT64, Field.Mode.REQUIRED)
    var User_ID: Int?,
    @BigQueryField(StandardSQLTypeName.STRING, Field.Mode.REQUIRED)
    var Age: String?,
    @BigQueryField(StandardSQLTypeName.STRING, Field.Mode.REQUIRED)
    var Gender: String?,
    @BigQueryField(StandardSQLTypeName.INT64, Field.Mode.REQUIRED)
    var Occupation: Int,
    @BigQueryField(StandardSQLTypeName.STRING, Field.Mode.REQUIRED)
    var City_Category: String?,
    @BigQueryField(StandardSQLTypeName.STRING, Field.Mode.REQUIRED)
    var Stay_In_Current_City_Years: String?,
    @BigQueryField(StandardSQLTypeName.INT64, Field.Mode.REQUIRED)
    var Marital_Status: Int?,
    @BigQueryField(StandardSQLTypeName.INT64, Field.Mode.REQUIRED)
    var Purchase_Count: Int,
    @BigQueryField(StandardSQLTypeName.INT64, Field.Mode.REQUIRED)
    var Purchase_Total: Int,
    @BigQueryField(StandardSQLTypeName.INT64, Field.Mode.REPEATED)
    var Product_Category_1: MutableSet<Int>,
    @BigQueryField(StandardSQLTypeName.INT64, Field.Mode.REPEATED)
    var Product_Category_2: MutableSet<Int>,
    @BigQueryField(StandardSQLTypeName.INT64, Field.Mode.REPEATED)
    var Product_Category_3: MutableSet<Int>,
    @BigQueryField(StandardSQLTypeName.STRING, Field.Mode.REQUIRED)
    var mlPartition: String,
    override var Encoded: MutableMap<String, Int>
) : Serializable, OutputBase() {

    constructor() : this(
        0,
        "",
        "",
        0,
        "",
        "",
        0,
        0,
        0,
        mutableSetOf<Int>(),
        mutableSetOf<Int>(),
        mutableSetOf<Int>(),
        "",
        HashMap<String, Int>()
    )

    companion object : OutputCompanionInterface {
        override fun getValueAsString(name: String, obj: OutputInterface): String? =
            OutputBase.getValueAsString(name, obj)

        override fun toTableRow(obj: OutputInterface): TableRow = OutputBase.toTableRow(obj)
    }

    override fun <T> makeCopy(): T? {
        return this.copy() as T
    }
}