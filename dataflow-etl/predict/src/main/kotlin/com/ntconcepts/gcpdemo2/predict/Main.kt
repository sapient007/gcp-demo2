package com.ntconcepts.gcpdemo2.predict

import com.ntconcepts.gcpdemo2.dataprep.transforms.BigQueryCreateTable
import com.ntconcepts.gcpdemo2.predict.io.BigQuerySchema
import com.ntconcepts.gcpdemo2.predict.models.UserSummaryPredict
import com.ntconcepts.gcpdemo2.predict.transforms.MapToKVsFn
import com.ntconcepts.gcpdemo2.predict.transforms.MapToTableRowsFn
import com.ntconcepts.gcpdemo2.predict.transforms.PredictFn
import org.apache.beam.sdk.Pipeline
import org.apache.beam.sdk.io.gcp.bigquery.BigQueryIO
import org.apache.beam.sdk.options.PipelineOptionsFactory
import org.apache.beam.sdk.transforms.ParDo

fun main(args: Array<String>) {
    val options = getOptions(args)
    val p = getPipeline(options)
    p.run()
}

fun getPipeline(options: PredictOptions): Pipeline {
    val p = Pipeline.create(options)

    //Get fields in order they appear in schema
    val schemaView = p.apply(BigQuerySchema(options.dataset, options.table))

    p.apply(
        "Create BQ Table",
        BigQueryCreateTable(
            options.dataset,
            options.outputUserPredictedTable,
            options.dropTable,
            UserSummaryPredict::class.java
        )
    )

    p.apply(
        "ReadValidationData",
        BigQueryIO.readTableRows()
            .withoutValidation()
            .from(options.userSummaryTableSpec)
            .withMethod(BigQueryIO.TypedRead.Method.DIRECT_READ)
            .withSelectedFields(getSelectedFields())
            .withRowRestriction("ml_partition = \"validate\"")
    ).apply(
        "MapTableRowsToSets",
        ParDo.of(MapToKVsFn(options.labelName, listOf("User_ID"))).withSideInput("schema", schemaView)
    ).apply(
        "PredictUserTotals",
        ParDo.of(PredictFn(options.project, options.modelId, options.modelVersionId))
    ).apply(
        ParDo.of(MapToTableRowsFn())
    ).apply(
        "WriteBigquery",
        BigQueryIO.writeTableRows()
            .to(options.outputUserPredictedTableSpec)
            .optimizedWrites()
            .withCreateDisposition(BigQueryIO.Write.CreateDisposition.CREATE_NEVER)
            .withWriteDisposition(BigQueryIO.Write.WriteDisposition.WRITE_TRUNCATE)
    )

    return p
}

fun getOptions(args: Array<String>): PredictOptions {
    return PipelineOptionsFactory.fromArgs(*args).withValidation()
        .`as`(PredictOptions::class.java)
}

fun getSelectedFields(): List<String> {
    val list = arrayListOf(
        "User_ID",
        "Purchase_Total",
        "Purchase_Count",
        "City_Category_a",
        "City_Category_b",
        "City_Category_c",
        "Gender_m",
        "Gender_f",
        "Age_0_17",
        "Age_18_25",
        "Age_26_35",
        "Age_36_45",
        "Age_46_50",
        "Age_51_55",
        "Age_55"
    )

    for (i in 0..20) {
        if (i < 10) {
            list.add(String.format("Occupation_%02d", i))
        } else {
            list.add("Occupation_$i")
        }
    }

    for (i in 1..4) {
        list.add("Stay_In_Current_City_Years_$i")
    }

    for (i in 1..20) {
        if (i < 10) {
            list.add(String.format("Product_Category_1_%02d", i))
        } else {
            list.add("Product_Category_1_$i")
        }
    }

    for (i in 1..18) {
        //These don't exist
        if (i == 1) continue
        if (i < 10) {
            list.add(String.format("Product_Category_2_%02d", i))
        } else {
            list.add("Product_Category_2_$i")
        }
    }

    for (i in 1..18) {
        //These don't exist
        if (i == 1 || i == 2 || i == 7) continue
        if (i < 10) {
            list.add(String.format("Product_Category_3_%02d", i))
        } else {
            list.add("Product_Category_3_$i")
        }
    }


    return list.toList()

}
