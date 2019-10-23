package com.ntconcepts.gcpdemo2.transforms

import com.ntconcepts.gcpdemo2.models.UserSummary
import com.ntconcepts.gcpdemo2.utils.RandomCollection
import org.apache.beam.sdk.options.ValueProvider
import org.apache.beam.sdk.transforms.DoFn

class SetMLPartitionsFn(
    private val mlPartitionTrainWeight: ValueProvider<Double>,
    private val mlPartitionTestWeight: ValueProvider<Double>,
    private val mlPartitionValidationWeight: ValueProvider<Double>
) :
    DoFn<UserSummary, UserSummary>() {

    private val weights = RandomCollection<String>()

    @StartBundle
    fun setupWeights() {
        if (weights.size() == 0) {
            weights.add(mlPartitionTrainWeight.get(), "train")
            weights.add(mlPartitionTestWeight.get(), "test")
            weights.add(mlPartitionValidationWeight.get(), "validate")
        }
    }

    @ProcessElement
    fun apply(c: ProcessContext) {
        val row = c.element().copy()
        row.ml_partition = weights.next()
        c.output(row)
    }


}