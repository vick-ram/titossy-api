package com.example.db.util

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ColumnType
import org.jetbrains.exposed.sql.Table

fun Table.tsVector(name: String): Column<String> = registerColumn(name, TsVectorColumnType())

class TsVectorColumnType : ColumnType() {
    override fun sqlType() = "TSVECTOR"
}