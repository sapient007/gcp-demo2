package com.ntconcepts.models

import com.ntconcepts.gcpdemo2.models.UserSummary
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class OutputBaseTests {

    @Test
    fun getValueAsString() {

        val user = UserSummary()
        user.Marital_Status = 1
        user.User_ID = null

        val ms = UserSummary.getValueAsString("Marital_Status", user)
        assertEquals("01", ms)

        val id = UserSummary.getValueAsString("User_ID", user)
        assertEquals("", id)
    }
}