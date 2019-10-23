package com.ntconcepts.gcpdemo2.transforms

import com.google.cloud.bigquery.*
import com.ntconcepts.gcpdemo2.utils.BigQueryField
import org.apache.beam.sdk.coders.BooleanCoder
import org.apache.beam.sdk.options.ValueProvider
import org.apache.beam.sdk.transforms.Create
import org.apache.beam.sdk.transforms.DoFn
import org.apache.beam.sdk.transforms.PTransform
import org.apache.beam.sdk.transforms.ParDo
import org.apache.beam.sdk.values.PBegin
import org.apache.beam.sdk.values.PCollectionView
import org.apache.beam.sdk.values.PDone
import kotlin.reflect.full.findAnnotation

class BigQueryCreateTable(
    val dataset: ValueProvider<String>,
    val table: ValueProvider<String>,
    val dropTable: ValueProvider<Boolean>,
    val encodedViewsMap: HashMap<String, PCollectionView<List<String>>>,
    val encodedViewsList: ArrayList<PCollectionView<List<String>>?>,
    val encodedCategories: Map<String, List<Class<*>>>,
    val clazz: Class<*>
) : PTransform<PBegin, PDone>() {


    override fun expand(input: PBegin): PDone {
        //Hack bc passing PBegin wasn't working with ParDo/DoFn
        input.apply(Create.of(true)).setCoder(BooleanCoder.of())
            .apply(
                "CreateTable",
                ParDo.of(
                    CreateBQTable(dataset, table, dropTable, encodedViewsMap, encodedCategories, clazz)
                ).withSideInputs(encodedViewsList)
            )
        return PDone.`in`(input.pipeline)
    }

    class CreateBQTable(
        val dataset: ValueProvider<String>,
        val table: ValueProvider<String>,
        val dropTable: ValueProvider<Boolean>,
        val encodedViewsMap: HashMap<String, PCollectionView<List<String>>>,
        val encodedCategories: Map<String, List<Class<*>>>,
        val clazz: Class<*>
    ) : DoFn<Boolean, Void>() {
        lateinit var bigquery: BigQuery

        @Setup
        fun setup() {
            if (!::bigquery.isInitialized) {
                bigquery = BigQueryOptions.getDefaultInstance().service
            }
        }

        @ProcessElement
        fun apply(c: ProcessContext) {
            val tableId = TableId.of(dataset.get(), table.get())
            if (dropTable.get()) {
                bigquery.delete(tableId)
            }
            val fields = ArrayList<Field>()


//            Class.forName(clazz.canonicalName).kotlin

            //Add fields with BigQueryField annotations
            clazz.kotlin.members.forEach {
                val fieldName = it.name
                val bqa = it.findAnnotation<BigQueryField>()
                if (bqa != null) {
                    fields.add(
                        Field.newBuilder(
                            fieldName,
                            bqa.sqlType
                        ).setMode(bqa.mode)
                            .build()
                    )
                }
            }

            //Add encoded fields
            encodedViewsMap.forEach {
                val fieldName = it.key
                val encodedDistinctVals = c.sideInput(it.value).sorted()

                encodedDistinctVals.forEach {
                    //Only add the encoded field to the schema if it's whitelisted
                    if ((encodedCategories[fieldName]
                            ?: error("Could not find $fieldName in list of encoded category fields")).contains(clazz)
                    ) {
                        fields.add(
                            Field.newBuilder(
                                it,
                                StandardSQLTypeName.INT64
                            ).setMode(Field.Mode.NULLABLE).build()
                        )
                    }
                }
            }

            val tableDefinition = StandardTableDefinition.of(Schema.of(fields))
            bigquery.create(TableInfo.of(tableId, tableDefinition))
        }
    }

}