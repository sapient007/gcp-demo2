package com.ntconcepts.gcpdemo2.predict.transforms

import com.google.common.base.MoreObjects.firstNonNull
import com.ntconcepts.gcpdemo2.predict.models.UserSummaryPredict
import org.apache.beam.sdk.options.ValueProvider
import org.apache.beam.sdk.state.BagState
import org.apache.beam.sdk.state.StateSpecs
import org.apache.beam.sdk.state.ValueState
import org.apache.beam.sdk.transforms.DoFn
import org.apache.beam.sdk.values.KV


class PredictFn(
    private val projectId: String,
    private val modelId: ValueProvider<String>,
    private val versionId: ValueProvider<String>
) : DoFn<KV<Int, String>, UserSummaryPredict>() {
    private val MAX_BUFFER_SIZE = 100

    @StateId("buffer")
    private val bufferedEvents = StateSpecs.bag<KV<Int, String>>()

    @StateId("count")
    private val countState = StateSpecs.value<Int>()

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

        if (count >= MAX_BUFFER_SIZE || context.pane().isLast()) {

            val json = predict(bufferState.read())
//            println(json)
//            for (enrichedEvent in enrichEvents(bufferState.read())) {
//                context.output(enrichedEvent)
//            }
            bufferState.clear()
            countState.clear()
        }
    }

    fun predict(buffered: Iterable<KV<Int, String>>): UserSummaryPredict {
        val json = makeJson(buffered)

//        PredictionRequest.predict(projectId, modelId.get(), versionId.get(), json)

        return UserSummaryPredict()

    }

    fun makeJson(buffered: Iterable<KV<Int, String>>): String {
        val instances = arrayListOf<String>()
        buffered.forEach {
            instances.add(it.value)
        }
        var inputs = "{ \"instances\": ["
        inputs += instances.joinToString(",")
        inputs += "]}"
        return inputs
    }
}