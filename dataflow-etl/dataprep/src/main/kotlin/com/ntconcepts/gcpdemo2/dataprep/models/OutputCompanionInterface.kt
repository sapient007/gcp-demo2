package com.ntconcepts.gcpdemo2.dataprep.models

import com.google.api.services.bigquery.model.TableRow

interface OutputCompanionInterface {
    fun getValueAsString(name: String, obj: OutputInterface): String?
    fun toTableRow(obj: OutputInterface): TableRow
}