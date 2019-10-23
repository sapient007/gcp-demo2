package com.ntconcepts.gcpdemo2.transforms

import com.google.cloud.storage.Blob
import com.google.cloud.storage.BlobId
import com.google.cloud.storage.StorageOptions
import com.ntconcepts.gcpdemo2.models.Purchase
import org.apache.beam.sdk.io.fs.MatchResult
import org.apache.beam.sdk.transforms.DoFn
import org.apache.beam.sdk.values.TupleTag
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStreamReader
import java.net.URI


class CSVParserFn(
    val usersOutput: TupleTag<Purchase>,
    val encodedOutputMap: HashMap<String, TupleTag<String>>
) : DoFn<MatchResult.Metadata, Purchase>() {

    private lateinit var storageOptions: StorageOptions

    @StartBundle
    fun start() {
        if (!::storageOptions.isInitialized) {
            storageOptions = StorageOptions.getDefaultInstance()
        }
    }

    @ProcessElement
    fun apply(c: ProcessContext, out: MultiOutputReceiver) {

        val file = c.element()

        val uri = URI.create(file.resourceId().toString())

        val blob = storageOptions.service.get(BlobId.of(uri.host, uri.path.removePrefix("/")))

        val baos = ByteArrayOutputStream()

        blob.downloadTo(
            baos,
            Blob.BlobSourceOption.generationMatch()
        )

        val reader = InputStreamReader(ByteArrayInputStream(baos.toByteArray()))
        val parser = CSVParser.parse(reader, CSVFormat.RFC4180.withFirstRecordAsHeader())

        parser.forEach {
            val purchase = Purchase(
                it.get(Purchase::User_ID.name).toInt(),
                it.get(Purchase::Product_ID.name),
                it.get(Purchase::Gender.name),
                it.get(Purchase::Age.name),
                it.get(Purchase::Occupation.name).toInt(),
                it.get(Purchase::City_Category.name),
                it.get(Purchase::Stay_In_Current_City_Years.name),
                it.get(Purchase::Marital_Status.name).toInt(),
                it.get(Purchase::Product_Category_1.name).toInt(),
                if (it.get(Purchase::Product_Category_2.name) != "") it.get(Purchase::Product_Category_2.name).toInt() else null,
                if (it.get(Purchase::Product_Category_3.name) != "") it.get(Purchase::Product_Category_3.name).toInt() else null,
                it.get(Purchase::Purchase.name).toInt()
            )

            //If any of these are null, drop the row. Nulls in categories 2 and 3 are OK
            if (purchase.User_ID == null ||
                purchase.Gender == null ||
                purchase.Age == null ||
                purchase.Occupation == null ||
                purchase.City_Category == null ||
                purchase.Stay_In_Current_City_Years == null ||
                purchase.Marital_Status == null ||
                purchase.Product_Category_1 == null
            ) {
                return
            }

            out.get(usersOutput).output(purchase)

            encodedOutputMap.forEach {
                val fieldName = it.key
                val tag = it.value

                val value = Purchase.getValueAsString(fieldName, purchase)
                if (value != "") {
                    out.get(tag).output(value)
                }

            }


        }
    }

}