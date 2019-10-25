package com.ntconcepts.gcpdemo2.predict.transforms

import com.google.common.base.MoreObjects.firstNonNull
import com.ntconcepts.gcpdemo2.predict.models.UserSummaryPredict
import org.apache.beam.sdk.state.BagState
import org.apache.beam.sdk.state.StateSpecs
import org.apache.beam.sdk.state.ValueState
import org.apache.beam.sdk.transforms.DoFn
import org.apache.beam.sdk.values.KV


class PredictFn : DoFn<KV<Int, String>, UserSummaryPredict>() {
    private val MAX_BUFFER_SIZE = 500

    @StateId("buffer")
    private val bufferedEvents = StateSpecs.bag<Any>()

    @StateId("count")
    private val countState = StateSpecs.value<Any>()

    @ProcessElement
    fun process(
        context: ProcessContext,
        @StateId("buffer") bufferState: BagState<KV<Int, String>>,
        @StateId("count") countState: ValueState<Int>
    ) {

        var count = firstNonNull(countState.read(), 0)
        count += 1
        countState.write(count)
        bufferState.add(context.element())

        if (count >= MAX_BUFFER_SIZE) {
//            for (enrichedEvent in enrichEvents(bufferState.read())) {
//                context.output(enrichedEvent)
//            }
            bufferState.clear()
            countState.clear()
        }
    }
}