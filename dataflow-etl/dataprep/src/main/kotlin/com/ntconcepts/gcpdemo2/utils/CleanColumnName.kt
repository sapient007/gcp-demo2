package com.ntconcepts.gcpdemo2.utils

object CleanColumnName {
    fun clean(name: String?): String {
        if (name == null) return ""
        return name.replace(Regex("\\s"), "_")
            .replace("-", "_")
            .replace(Regex("[^A-Za-z0-9_]"), "")
            .toLowerCase()
    }
}