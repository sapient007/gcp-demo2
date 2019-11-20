package com.ntconcepts.gcpdemo2.dataprep.transforms

import com.google.cloud.bigquery.*
import com.ntconcepts.gcpdemo2.dataprep.utils.BigQueryField
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
    val encodedViewsMap: HashMap<String, PCollectionView<List<String>>>?,
    val encodedViewsList: ArrayList<PCollectionView<List<String>>?>?,
    val encodedCategories: Map<String, List<Class<*>>>?,
    val clazz: Class<*>
) : PTransform<PBegin, PDone>() {

    constructor(
        dataset: ValueProvider<String>,
        table: ValueProvider<String>,
        dropTable: ValueProvider<Boolean>,
        clazz: Class<*>
    ) : this(
        dataset,
        table,
        dropTable,
        null,
        null,
        null,
        clazz
    )

    override fun expand(input: PBegin): PDone {

        //withSideInputs must be added declaratively or else the DAG
        //will not be constructed correctly and this transform
        //will not get the side inputs
        if (encodedViewsList != null) {
            input.apply(Create.of(true)).setCoder(BooleanCoder.of())
                .apply(
                    "CreateTable",
                    ParDo.of(
                        CreateBQTable(dataset, table, dropTable, encodedViewsMap, encodedCategories, clazz)
                    ).withSideInputs(encodedViewsList)
                )
        } else {
            input.apply(Create.of(true)).setCoder(BooleanCoder.of())
                .apply(
                    "CreateTable",
                    ParDo.of(
                        CreateBQTable(dataset, table, dropTable, encodedViewsMap, encodedCategories, clazz)
                    )
                )
        }
        //Hack bc passing PBegin wasn't working with ParDo/DoFn

        return PDone.`in`(input.pipeline)
    }

    class CreateBQTable(
        val dataset: ValueProvider<String>,
        val table: ValueProvider<String>,
        val dropTable: ValueProvider<Boolean>,
        val encodedViewsMap: HashMap<String, PCollectionView<List<String>>>?,
        val encodedCategories: Map<String, List<Class<*>>>?,
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
            encodedViewsMap?.forEach {
                val fieldName = it.key
                val encodedDistinctVals = c.sideInput(it.value)

                encodedDistinctVals.sorted().forEach {
                    //Only add the encoded field to the schema if it's whitelisted
                    if ((encodedCategories!![fieldName]
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