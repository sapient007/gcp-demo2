package com.ntconcepts.gcpdemo2.dataprep.transforms

import com.ntconcepts.gcpdemo2.dataprep.models.Purchase
import org.apache.beam.sdk.transforms.Contextful
import org.apache.beam.sdk.values.KV

class MapPurchasesToKVContexful : Contextful.Fn<Purchase, KV<Int, Purchase>> {
    override fun apply(purchase: Purchase, c: Contextful.Fn.Context): KV<Int, Purchase> {
        return KV.of(purchase.User_ID, purchase)
    }
}