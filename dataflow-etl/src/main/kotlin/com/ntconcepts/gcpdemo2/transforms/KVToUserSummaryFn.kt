package com.ntconcepts.gcpdemo2.transforms

import com.ntconcepts.gcpdemo2.models.UserSummary
import org.apache.beam.sdk.transforms.SimpleFunction
import org.apache.beam.sdk.values.KV

class KVToUserSummaryFn : SimpleFunction<KV<Int, UserSummary>, UserSummary>() {
    override fun apply(kv: KV<Int, UserSummary>): UserSummary {
        return kv.value
    }
}