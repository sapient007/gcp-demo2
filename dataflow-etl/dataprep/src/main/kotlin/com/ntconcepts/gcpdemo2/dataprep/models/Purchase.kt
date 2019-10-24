package com.ntconcepts.gcpdemo2.dataprep.models

import com.google.api.services.bigquery.model.TableRow
import com.google.cloud.bigquery.Field
import com.google.cloud.bigquery.StandardSQLTypeName
import com.ntconcepts.gcpdemo2.dataprep.utils.BigQueryField
import java.io.Serializable

data class Purchase(
    @BigQueryField(StandardSQLTypeName.INT64, Field.Mode.REQUIRED)
    val User_ID: Int?,
    @BigQueryField(StandardSQLTypeName.STRING, Field.Mode.REQUIRED)
    val Product_ID: String?,
    @BigQueryField(StandardSQLTypeName.STRING, Field.Mode.REQUIRED)
    val Gender: String?,
    @BigQueryField(StandardSQLTypeName.STRING, Field.Mode.REQUIRED)
    val Age: String?,
    @BigQueryField(StandardSQLTypeName.INT64, Field.Mode.REQUIRED)
    val Occupation: Int?,
    @BigQueryField(StandardSQLTypeName.STRING, Field.Mode.REQUIRED)
    val City_Category: String?,
    @BigQueryField(StandardSQLTypeName.STRING, Field.Mode.REQUIRED)
    val Stay_In_Current_City_Years: String?,
    @BigQueryField(StandardSQLTypeName.INT64, Field.Mode.REQUIRED)
    val Marital_Status: Int?,
    @BigQueryField(StandardSQLTypeName.INT64, Field.Mode.REQUIRED)
    val Product_Category_1: Int?,
    @BigQueryField(StandardSQLTypeName.INT64, Field.Mode.NULLABLE)
    val Product_Category_2: Int?,
    @BigQueryField(StandardSQLTypeName.INT64, Field.Mode.NULLABLE)
    val Product_Category_3: Int?,
    @BigQueryField(StandardSQLTypeName.INT64, Field.Mode.REQUIRED)
    val Purchase: Int,
    @BigQueryField(StandardSQLTypeName.STRING, Field.Mode.REQUIRED)
    var mlPartition: String,
    //Mutable so it can be copied in DoFns
    override var Encoded: MutableMap<String, Int>
) : Serializable, OutputBase() {
    constructor() : this(
        0,
        "",
        "",
        "0",
        0,
        "",
        "",
        0,
        null,
        null,
        null,
        0,
        "",
        HashMap<String, Int>()
    )

    constructor(
        User_ID: Int?,
        Product_ID: String?,
        Gender: String?,
        Age: String?,
        Occupation: Int,
        City_Category: String?,
        Stay_In_Current_City_Years: String?,
        Marital_Status: Int?,
        Product_Category_1: Int?,
        Purchase: Int
    ) : this(
        User_ID,
        Product_ID,
        Gender,
        Age,
        Occupation,
        City_Category,
        Stay_In_Current_City_Years,
        Marital_Status,
        Product_Category_1,
        null,
        null,
        Purchase,
        "",
        HashMap<String, Int>()
    )

    constructor(
        User_ID: Int?,
        Product_ID: String?,
        Gender: String?,
        Age: String?,
        Occupation: Int,
        City_Category: String?,
        Stay_In_Current_City_Years: String?,
        Marital_Status: Int?,
        Product_Category_1: Int?,
        Product_Category_2: Int?,
        Product_Category_3: Int?,
        Purchase: Int
    ) : this(
        User_ID,
        Product_ID,
        Gender,
        Age,
        Occupation,
        City_Category,
        Stay_In_Current_City_Years,
        Marital_Status,
        Product_Category_1,
        Product_Category_2,
        Product_Category_3,
        Purchase,
        "",
        HashMap<String, Int>()
    )

    constructor(User_ID: Int?) : this(
        User_ID,
        "",
        "",
        "0",
        0,
        "",
        "",
        0,
        null,
        null,
        null,
        0,
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