package com.ntconcepts.gcpdemo2.dataprep.models

interface OutputInterface {

    var Encoded: MutableMap<String, Int>

    //This is pug fugly but copy() is autogenerated based on class constructor and can't be inherited
    fun <T> makeCopy(): T?

}