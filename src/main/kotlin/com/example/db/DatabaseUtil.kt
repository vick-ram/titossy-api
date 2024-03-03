package com.example.db

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseUtil {
    fun init() {
        Database.connect(hikari())
        transaction {
            SchemaUtils.create(CustomerTable)
        }
    }

    suspend fun <T> dbQuery(block: () -> T): T {
        return newSuspendedTransaction(Dispatchers.IO) { block() }
    }
}

fun hikari(): HikariDataSource {
    val config = HikariConfig()
        .apply {
            dataSourceClassName = "org.postgresql.ds.PGSimpleDataSource"
            addDataSourceProperty("databaseName", "titossy-cleaning-services")
            addDataSourceProperty("portNumber", 5432)
            addDataSourceProperty("serverName", "localhost")
            addDataSourceProperty("user", "postgres")
            addDataSourceProperty("password", "Vickram9038")

            maximumPoolSize = 3
            isAutoCommit = false
            transactionIsolation = "TRANSACTION_REPEATABLE_READ"
            validate()
        }

    return HikariDataSource(config)
}