package com.ntconcepts.gcpdemo2.accumulators

import com.ntconcepts.gcpdemo2.models.Purchase
import com.ntconcepts.gcpdemo2.models.UserSummary
import org.apache.beam.sdk.transforms.Combine

class UserSummaryFn : Combine.CombineFn<Purchase, UserSummary, UserSummary>() {

    override fun createAccumulator(): UserSummary {
        return UserSummary()
    }

    override fun addInput(accum: UserSummary, input: Purchase): UserSummary {
        accum.Age = input.Age
        accum.Gender = input.Gender
        accum.City_Category = input.City_Category
        accum.Marital_Status = input.Marital_Status
        accum.Occupation = input.Occupation
        accum.Stay_In_Current_City_Years = input.Stay_In_Current_City_Years
        accum.User_ID = input.User_ID
        accum.mlPartition = input.mlPartition
        accum.Purchase_Total += input.Purchase
        accum.Purchase_Count++
        if (input.Product_Category_1 != null) accum.Product_Category_1.add(input.Product_Category_1)
        if (input.Product_Category_2 != null) accum.Product_Category_2.add(input.Product_Category_2)
        if (input.Product_Category_3 != null) accum.Product_Category_3.add(input.Product_Category_3)
        return accum
    }

    override fun mergeAccumulators(accums: Iterable<UserSummary>): UserSummary {
        var merged = createAccumulator()

        for (accum in accums) {
            merged.Age = accum.Age
            merged.Gender = accum.Gender
            merged.City_Category = accum.City_Category
            merged.Marital_Status = accum.Marital_Status
            merged.Occupation = accum.Occupation
            merged.Stay_In_Current_City_Years = accum.Stay_In_Current_City_Years
            merged.User_ID = accum.User_ID
            merged.mlPartition = accum.mlPartition
            merged.Purchase_Total += accum.Purchase_Total
            merged.Purchase_Count += accum.Purchase_Count

            merged.Product_Category_1.addAll(accum.Product_Category_1)
            merged.Product_Category_2.addAll(accum.Product_Category_2)
            merged.Product_Category_3.addAll(accum.Product_Category_3)
        }

        return merged
    }

    override fun extractOutput(accum: UserSummary): UserSummary {
        return accum
    }
}