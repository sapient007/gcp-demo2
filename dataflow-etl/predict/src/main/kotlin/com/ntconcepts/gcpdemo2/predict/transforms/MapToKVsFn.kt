package com.ntconcepts.gcpdemo2.predict.transforms

import com.google.api.services.bigquery.model.TableRow
import com.google.cloud.bigquery.Field
import com.google.cloud.bigquery.LegacySQLTypeName
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonPrimitive
import org.apache.beam.sdk.options.ValueProvider
import org.apache.beam.sdk.transforms.DoFn
import org.apache.beam.sdk.values.KV
import java.util.*

class MapToKVsFn(
    private val labelName: ValueProvider<String>,
    private val blacklisted: List<String>
) :
    DoFn<TableRow, KV<Int, KV<Int, String>>>() {

    private lateinit var gson: Gson

    @Setup
    fun setup() {
        if (!::gson.isInitialized) {
            gson = Gson()
        }
    }

    @ProcessElement
    fun apply(c: ProcessContext, @SideInput("schema") schema: LinkedHashMap<String, Field>) {

        val row = c.element()

        val out = JsonArray()

        //Add the label value first
        out.add(JsonPrimitive((row[labelName.get()] as String).toInt()))

        row.forEach {
            val fieldName = it.key
            //Don't add the label again and don't add blacklisted cols

            if (it.key != labelName.get() && blacklisted.find { s -> s == fieldName } == null) {
                //Smart cast based on schema type. All values come through as strings
                when (schema[it.key]?.type) {
                    LegacySQLTypeName.FLOAT -> out.add(JsonPrimitive((it.value as String).toDouble()))
                    LegacySQLTypeName.INTEGER, LegacySQLTypeName.NUMERIC -> out.add(JsonPrimitive((it.value as String).toInt()))
                    LegacySQLTypeName.STRING -> out.add(JsonPrimitive((it.value as String)))
                }
            }
        }
        //We want to batch our prediction requests so the keys must all be 0
        //otherwise it only groups in PredictFn by UserID, so only one user
        //per prediction api request.
        //However, we also want to maintain our UserID -> feature pairing to
        //combine predictions with UserIDs.
        c.output(
            KV.of(
                0,
                KV.of(row["User_ID"].toString().toInt(), gson.toJson(out))
            )
        )

    }
}