package com.ntconcepts.gcpdemo2.dataprep

import com.google.api.services.bigquery.model.TableRow
import com.google.common.collect.ImmutableSet
import com.ntconcepts.gcpdemo2.dataprep.accumulators.UserSummaryFn
import com.ntconcepts.gcpdemo2.dataprep.models.Purchase
import com.ntconcepts.gcpdemo2.dataprep.models.UserSummary
import com.ntconcepts.gcpdemo2.dataprep.transforms.*
import com.ntconcepts.gcpdemo2.dataprep.utils.CleanColumnName
import org.apache.beam.sdk.Pipeline
import org.apache.beam.sdk.coders.KvCoder
import org.apache.beam.sdk.coders.SerializableCoder
import org.apache.beam.sdk.coders.StringUtf8Coder
import org.apache.beam.sdk.coders.VarIntCoder
import org.apache.beam.sdk.io.FileIO
import org.apache.beam.sdk.io.gcp.bigquery.BigQueryIO
import org.apache.beam.sdk.options.PipelineOptionsFactory
import org.apache.beam.sdk.transforms.*
import org.apache.beam.sdk.values.*

//val encodedCategories = listOf<String>(
//    Purchase::Gender.name,
//    Purchase::Age.name,
//    Purchase::Occupation.name,
//    Purchase::City_Category.name,
//    Purchase::Stay_In_Current_City_Years.name
////    Purchase::Product_Category_1.name
//)

//Map of raw data field names to generate distinct values on and white list of classes that will hot encode those fields
val encodedCategories = mapOf<String, List<Class<*>>>(
    Pair(
        Purchase::Gender.name,
        listOf(UserSummary::class.java)
    ),
    Pair(
        Purchase::Age.name,
        listOf(UserSummary::class.java)
    ),
    Pair(
        Purchase::Occupation.name,
        listOf(UserSummary::class.java)
    ),
    Pair(
        Purchase::City_Category.name,
        listOf(UserSummary::class.java)
    ),
    Pair(
        Purchase::Stay_In_Current_City_Years.name,
        listOf(UserSummary::class.java)
    ),
    Pair(
        Purchase::Product_Category_1.name,
        listOf(UserSummary::class.java)
    ),
    Pair(
        Purchase::Product_Category_2.name,
        listOf(UserSummary::class.java)
    ),
    Pair(
        Purchase::Product_Category_3.name,
        listOf(UserSummary::class.java)
    )
)

fun main(args: Array<String>) {
    val options = getOptions(args)
    val p = getPipeline(options)
    p.run()
}

fun getOptions(args: Array<String>): com.ntconcepts.gcpdemo2.dataprep.Demo2Options {
    return PipelineOptionsFactory.fromArgs(*args).withValidation()
        .`as`(com.ntconcepts.gcpdemo2.dataprep.Demo2Options::class.java)
}

fun makeEncodedOutputs(encodedOutputMap: HashMap<String, TupleTag<String>>): TupleTagList {

    val tags = mutableListOf<TupleTag<*>>()

    encodedCategories.forEach {
        val tag = TupleTag<String>()
        encodedOutputMap[it.key] = tag
        tags.add(tag)
    }

    return TupleTagList.of(tags)

}

fun getPipeline(options: com.ntconcepts.gcpdemo2.dataprep.Demo2Options): Pipeline {
    val p = Pipeline.create(options)

    val encodedOutputMap = HashMap<String, TupleTag<String>>()
    val outputTags = makeEncodedOutputs(encodedOutputMap)

//    val testUsersTag = TupleTag<User>()
//    val test = p.apply(
//        "Get testing data",
//        FileIO.match()
//            .filepattern(options.testDataSource)
//    )
//        .apply(
//            "Parse CSV",
//            ParDo.of(CSVParserFn("test", testUsersTag, encodedOutputMap))
//                .withOutputTags(testUsersTag, outputTags)
//        )
//    val testUsersPCollection: PCollection<User> =
//        test.get(testUsersTag).setCoder(SerializableCoder.of(User::class.java))

    val trainUsersTag = TupleTag<Purchase>()
    val train = p.apply(
        "Get training data",
        FileIO.match()
            .filepattern(options.trainDataSource)
    )
        .apply(
            "Parse CSV",
            ParDo.of(CSVParserFn(trainUsersTag, encodedOutputMap))
                .withOutputTags(trainUsersTag, outputTags)
        )
    val trainUsersPCollection: PCollection<Purchase> =
        train.get(trainUsersTag).setCoder(SerializableCoder.of(Purchase::class.java))

    val collectionList = PCollectionList.of(trainUsersPCollection)

    val users = collectionList.apply(Flatten.pCollections()).setCoder(SerializableCoder.of(Purchase::class.java))

    val encodedViewsPair = makeOutputViews(train, encodedOutputMap)

//    val purchases = users.apply(
//        "Map encoded values",
//        ParDo.of(EncodeOneHotFn(encodedViewsPair.first)).withSideInputs(encodedViewsPair.second)
//    )

    writePurchasesToBQ(p, options, users, encodedViewsPair)

    val userSummaries = groupUsers(users)


    writeUserSummariesToBQ(
        p,
        options,
        userSummaries.apply(
            "Map to UserSummaries", MapElements.into(
                TypeDescriptor.of(UserSummary::class.java)
            )
                .via(KVToUserSummaryFn())
        ).apply(
            "Make ML partitions",
            ParDo.of(
                SetMLPartitionsFn(
                    options.mlPartitionTrainWeight,
                    options.mlPartitionTestWeight,
                    options.mlPartitionValidationWeight
                )
            )
        )
            .apply(
                "EncodeProductCategories",
                ParDo.of(EncodeUserCategoriesFn(encodedViewsPair.first))
                    .withSideInputs(encodedViewsPair.second)
            )
            .apply(
                "Map encoded values",
                ParDo.of(
                    EncodeOneHotFn<UserSummary>(
                        encodedViewsPair.first, ImmutableSet.builder<String>()
                            .add(UserSummary::Product_Category_1.name)
                            .add(UserSummary::Product_Category_2.name)
                            .add(UserSummary::Product_Category_3.name)
                            .build()
                    )
                ).withSideInputs(encodedViewsPair.second)
            ).setCoder(SerializableCoder.of(UserSummary::class.java)),
        encodedViewsPair
    )


    //read in test and train data, union
    //drop unused cols
    //Get unique vals for categorical cols
    //get total purchases by user id
    //get total purchase amount by user id
    //one-hot encode categorical cols
    //drop unused cols, again

    return p

}

fun groupUsers(purchases: PCollection<Purchase>): PCollection<KV<Int, UserSummary>> {

    //Create UserSummaries
    return purchases.apply(
        "Map to KVs", ParDo.of(MapPurchasesToKVFn())
    ).apply(
        "Group by User_ID",
        GroupByKey.create<Int, Purchase>()
    ).apply(
        "Combine to UserSummary",
        Combine.groupedValues<Int, Purchase, UserSummary>(UserSummaryFn())
    ).setCoder(KvCoder.of(VarIntCoder.of(), SerializableCoder.of(UserSummary::class.java)))

}

fun writePurchasesToBQ(
    p: Pipeline,
    options: com.ntconcepts.gcpdemo2.dataprep.Demo2Options,
    purchases: PCollection<Purchase>,
    encodedViewsPair: Pair<HashMap<String, PCollectionView<List<String>>>, ArrayList<PCollectionView<List<String>>?>>
) {
    //Create or recreate table
    p.apply(
        "Create BQ Table",
        BigQueryCreateTable(
            options.outputDataset,
            options.outputPurchaseTable,
            options.dropTable,
            encodedViewsPair.first,
            encodedViewsPair.second,
            encodedCategories,
            Purchase::class.java
        )
    )

    purchases.apply(
        "Map to TableRows", MapElements.into(
            TypeDescriptor.of(TableRow::class.java)
        )
            .via(OutputPurchaseBigQueryFn())
    )
        .apply(
            "Write Bigquery",
            BigQueryIO.writeTableRows()
                .to(options.outputPurchaseTableSpec)
                .withCreateDisposition(BigQueryIO.Write.CreateDisposition.CREATE_NEVER)
                .withWriteDisposition(BigQueryIO.Write.WriteDisposition.WRITE_TRUNCATE)
        )
}

fun writeUserSummariesToBQ(
    p: Pipeline,
    options: com.ntconcepts.gcpdemo2.dataprep.Demo2Options,
    users: PCollection<UserSummary>,
    encodedViewsPair: Pair<HashMap<String, PCollectionView<List<String>>>, ArrayList<PCollectionView<List<String>>?>>
) {
    //Create or recreate table
    p.apply(
        "Create BQ Table",

        BigQueryCreateTable(
            options.outputDataset,
            options.outputUserSummaryTable,
            options.dropTable,
            encodedViewsPair.first,
            encodedViewsPair.second,
            encodedCategories,
            UserSummary::class.java
        )
    )

    users.apply(
        "Map to TableRows", MapElements.into(
            TypeDescriptor.of(TableRow::class.java)
        )
            .via(OutputUserSummaryBigQueryFn())
    )
        .apply(
            "Write Bigquery",
            BigQueryIO.writeTableRows()
                .to(options.outputUserSummaryTableSpec)
                .withCreateDisposition(BigQueryIO.Write.CreateDisposition.CREATE_NEVER)
                .withWriteDisposition(BigQueryIO.Write.WriteDisposition.WRITE_TRUNCATE)
        )
}

fun makeOutputViews(pTrain: PCollectionTuple, encodedOutputMap: HashMap<String, TupleTag<String>>): Pair<
        HashMap<String, PCollectionView<List<String>>>,
        ArrayList<PCollectionView<List<String>>?>
        > {

    val encodedViews = HashMap<String, PCollectionView<List<String>>>()
    val encodedViewsList = arrayListOf<PCollectionView<List<String>>?>()

    encodedOutputMap.forEach {
        //        val testCollection: PCollection<String> = pTest.get<String>(it.value).setCoder(StringUtf8Coder.of())
        val trainCollection: PCollection<String> = pTrain.get<String>(it.value).setCoder(StringUtf8Coder.of())

        val collectionList = PCollectionList.of(trainCollection)

        val merged = collectionList.apply(Flatten.pCollections()).setCoder(StringUtf8Coder.of())

        val fieldName = it.key

        encodedViews[fieldName] = merged.apply(
            Distinct.create<String>()
        )
            .apply(
                "Map field name as prefix",
                MapElements.into(TypeDescriptors.strings()).via(SerializableFunction<String, String> {
                    "${fieldName}_${CleanColumnName.clean(it)}"
                })
            )
            .apply("$fieldName View", View.asList<String>())

        encodedViewsList.add(encodedViews[fieldName])
    }

    return Pair(encodedViews, encodedViewsList)

}