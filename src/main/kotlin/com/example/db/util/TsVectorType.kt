package com.example.db.util

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ColumnType
import org.jetbrains.exposed.sql.Table
import org.postgresql.util.PGobject

/*fun Table.tsVector(name: String): Column<String> = registerColumn(name, TsVectorColumnType())

class TsVectorColumnType : ColumnType() {
    override fun sqlType() = "TSVECTOR"
}*/

class TsVectorColumnType : ColumnType() {
    override fun sqlType() = "TSVECTOR"

    override fun valueFromDB(value: Any): Any {
        return when (value) {
            is PGobject -> value.value ?: ""
            else -> value
        }
    }

    override fun notNullValueToDB(value: Any): Any {
        return when (value) {
            is String -> PGobject().also {
                it.type = "tsvector"
                it.value = value
            }

            else -> value
        }
    }
}

fun Table.tsVector(name: String): Column<String> = registerColumn(name, TsVectorColumnType())
