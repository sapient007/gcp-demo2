package com.ntconcepts.gcpdemo2.predict.io

import com.google.cloud.bigquery.*
import org.apache.beam.sdk.coders.BooleanCoder
import org.apache.beam.sdk.options.ValueProvider
import org.apache.beam.sdk.transforms.*
import org.apache.beam.sdk.values.PBegin
import org.apache.beam.sdk.values.PCollectionView

class BigQuerySchema(
    private val dataset: ValueProvider<String>,
    private val table: ValueProvider<String>
) : PTransform<PBegin, PCollectionView<LinkedHashMap<String, Field>>>() {

    override fun expand(input: PBegin): PCollectionView<LinkedHashMap<String, Field>> {
        //Hack bc passing PBegin wasn't working with ParDo/DoFn
        return input.apply(Create.of(true)).setCoder(BooleanCoder.of())
            .apply(
                "GetSchema",
                ParDo.of(
                    GetSchemaFn(dataset, table)
                )
            )
//            .setCoder(SetCoder.of(SerializableCoder.of(Field::class.java)))
            .apply(View.asSingleton<LinkedHashMap<String, Field>>())
    }

    class GetSchemaFn(
        private val dataset: ValueProvider<String>,
        private val table: ValueProvider<String>
    ) : DoFn<Boolean, LinkedHashMap<String, Field>>() {
        lateinit var bigquery: BigQuery

        @Setup
        fun setup() {
            if (!::bigquery.isInitialized) {
                bigquery = BigQueryOptions.getDefaultInstance().service
            }
        }

        @ProcessElement
        fun apply(c: ProcessContext) {
            val fields = linkedMapOf<String, Field>()
            val tableId = TableId.of(dataset.get(), table.get())

            val schema =
                bigquery.getDataset(dataset.get()).get(table.get()).getDefinition<StandardTableDefinition>().schema
            schema?.fields?.forEach {
                fields.put(it.name, it)
            }

            c.output(fields)
        }
    }
}