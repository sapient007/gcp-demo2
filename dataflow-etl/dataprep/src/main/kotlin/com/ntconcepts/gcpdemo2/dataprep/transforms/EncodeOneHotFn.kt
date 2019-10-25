package com.ntconcepts.gcpdemo2.dataprep.transforms

import com.google.common.collect.ImmutableSet
import com.ntconcepts.gcpdemo2.dataprep.models.OutputBase
import com.ntconcepts.gcpdemo2.dataprep.models.OutputInterface
import com.ntconcepts.gcpdemo2.dataprep.utils.CleanColumnName
import org.apache.beam.sdk.transforms.DoFn
import org.apache.beam.sdk.values.PCollectionView

class EncodeOneHotFn<T>(
    private val encodedViews: HashMap<String, PCollectionView<List<String>>>,
    private val encodedBlacklist: ImmutableSet<String>
) : DoFn<OutputBase, T>() {

    @ProcessElement
    fun apply(c: ProcessContext) {

        //We have to copy both of these because of immutability
        var user = c.element().makeCopy<T>() as OutputInterface
        user.Encoded = user.Encoded.toMutableMap()

        encodedViews.forEach {
            //Bring in distinct vals from side input
            val encodedDistinctVals = c.sideInput(it.value)
            val fieldName = it.key
            //Get field value via reflection
            val fieldValue = OutputBase.getValueAsString(fieldName, user)

            if (fieldValue != null && encodedBlacklist.find { it == fieldName } == null) {
                //Set encoded fields
                encodedDistinctVals.forEach {
                    user.Encoded["$it"] = if (it == "${fieldName}_${CleanColumnName.clean(fieldValue)}") {
                        1
                    } else {
                        0
                    }
                }

            }
        }

        c.output(user as T)
    }
}