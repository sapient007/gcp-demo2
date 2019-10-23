package com.ntconcepts.gcpdemo2.transforms

import com.ntconcepts.gcpdemo2.models.Purchase
import org.apache.beam.sdk.transforms.DoFn
import org.apache.beam.sdk.values.KV

//private val usersOut: TupleTag<UserSummary>
class MapPurchasesToKVFn() : DoFn<Purchase, KV<Int, Purchase>>() {

    @ProcessElement
    fun apply(c: ProcessContext) {
        val purchase = c.element()
        c.output(KV.of(purchase.User_ID, purchase))
    }

}