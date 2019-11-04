package com.ntconcepts.gcpdemo2.predict.transforms

import com.google.common.base.MoreObjects.firstNonNull
import com.google.gson.Gson
import com.ntconcepts.gcpdemo2.predict.models.PredictionResponse
import com.ntconcepts.gcpdemo2.predict.models.UserSummaryPredict
import com.ntconcepts.gcpdemo2.predict.utils.mlengine.PredictionRequest
import org.apache.beam.sdk.options.ValueProvider
import org.apache.beam.sdk.state.*
import org.apache.beam.sdk.transforms.DoFn
import org.apache.beam.sdk.values.KV


class PredictFn(
    private val projectId: String,
    private val modelId: ValueProvider<String>,
    private val versionId: ValueProvider<String>
) : DoFn<KV<Int, KV<Int, String>>, UserSummaryPredict>() {
    private val MAX_BUFFER_SIZE = 1000

    @StateId("buffer")
    private val bufferedEvents = StateSpecs.bag<KV<Int, KV<Int, String>>>()

    @StateId("count")
    private val countState = StateSpecs.value<Int>()

    @TimerId("expiry")
    private val expirySpec = TimerSpecs.timer(TimeDomain.EVENT_TIME)

    private lateinit var gson: Gson

    @Setup
    fun setup() {
        if (!::gson.isInitialized) {
            gson = Gson()
        }
    }

    @ProcessElement
    fun process(
        context: ProcessContext,
        @StateId("buffer") bufferState: BagState<KV<Int, KV<Int, String>>>,
        @StateId("count") countState: ValueState<Int>,
        @TimerId("expiry") expiryTimer: Timer
    ) {
        expiryTimer.setRelative()

        var count = firstNonNull(countState.read(), 0)
        count += 1

        countState.write(count)
        bufferState.add(context.element())

        if (count >= MAX_BUFFER_SIZE) {
            predict(context, bufferState.read())
            bufferState.clear()
            countState.clear()
        }
    }

    @OnTimer("expiry")
    fun onExpiry(
        context: OnTimerContext,
        @StateId("buffer") bufferState: BagState<KV<Int, KV<Int, String>>>
    ) {
        if (!bufferState.isEmpty.read()!!) {
            predict(context, bufferState.read())
            bufferState.clear()
        }
    }

    private fun predict(c: WindowedContext, buffered: Iterable<KV<Int, KV<Int, String>>>) {

        //Convert into a LinkedHashMap to preserve ordering. This will be vital
        //to join predicted values back to User_IDs
        val map = linkedMapOf<Int, String>()
        buffered.forEach {
            map[it.value.key!!] = it.value.value!!
        }
        if (map.size == 0) return

        val json = makeJson(map)

        val response: String?

        try {
            response = PredictionRequest.predict(projectId, modelId.get(), versionId.get(), json)
        } catch(e: Exception) {
            throw e
        }
        if (response == null) {
            error("Prediction response was null")
        }
        val predictions = parseResponse(response)
        if (predictions.predictions.isNullOrEmpty()) {
            error("Predictions were null. Response: $response")
        }

        predictions.predictions.forEachIndexed { index, i ->
            c.output(UserSummaryPredict(map.entries.elementAt(index).key, i))
        }




    }

    private fun parseResponse(json: String): PredictionResponse<Int> {
        return gson.fromJson<PredictionResponse<Int>>(json, PredictionResponse::class.java)
    }

    private fun makeJson(buffered: LinkedHashMap<Int, String>): String {
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