package com.ntconcepts.gcpdemo2.dataprep.transforms

import com.ntconcepts.gcpdemo2.dataprep.models.Purchase
import com.ntconcepts.gcpdemo2.dataprep.models.UserSummary
import org.apache.beam.sdk.transforms.DoFn
import org.apache.beam.sdk.values.PCollectionView

class EncodeUserCategoriesFn(private val encodedViews: HashMap<String, PCollectionView<List<String>>>) :
    DoFn<UserSummary, UserSummary>() {

    @ProcessElement
    fun apply(c: ProcessContext) {

        //Copy inputs
        val user = c.element()
        user.Encoded = user.Encoded.toMutableMap()

        hotEncodeCategories(
            Purchase::Product_Category_1.name,
            user.Encoded,
            user.Product_Category_1,
            c.sideInput(encodedViews[Purchase::Product_Category_1.name])
        )
        hotEncodeCategories(
            Purchase::Product_Category_2.name,
            user.Encoded,
            user.Product_Category_2,
            c.sideInput(encodedViews[Purchase::Product_Category_2.name])
        )
        hotEncodeCategories(
            Purchase::Product_Category_3.name,
            user.Encoded,
            user.Product_Category_3,
            c.sideInput(encodedViews[Purchase::Product_Category_3.name])
        )

        val purchase = c.element()
        c.output(user)
    }

    private fun hotEncodeCategories(
        fieldName: String,
        userEncoded: MutableMap<String, Int>,
        userCat: Set<Int>,
        distinctCats: List<String>
    ) {

        val encodedVals = mutableSetOf<String>()

        //Set each category the user has purchased to 1
        userCat.forEach {
            if (it != null) {
                val cleanedVal = if (it < 10) {
                    "%02d".format(it)
                } else {
                    it.toString()
                }
                val encodedName = "${fieldName}_$cleanedVal"
                encodedVals.add(encodedName)
                userEncoded[encodedName] = 1
            }
        }

        //All other categories are set to 0
        distinctCats.forEach {
            val distinctName = it
            if (encodedVals.find { it == distinctName } == null) {
                userEncoded[it] = 0
            }
        }


    }

}