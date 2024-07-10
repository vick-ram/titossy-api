package com.example.commands
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.QueryBuilder
import org.jetbrains.exposed.sql.append

fun Column<String>.customMatch(pattern: String): Op<Boolean> =
    object : Op<Boolean>() {
        override fun toQueryBuilder(queryBuilder: QueryBuilder) =
            queryBuilder {
                append(this@customMatch, " @@ to_tsquery('", pattern, ":*')")
            }
    }












