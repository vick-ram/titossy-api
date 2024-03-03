package com.example.commands.repo.customer

import com.example.auth.Config
import com.example.auth.TokePair
import com.example.auth.TokenBlackList
import com.example.auth.generateTokens
import com.example.db.CustomerTable
import com.example.db.DatabaseUtil.dbQuery
import com.example.models.Customer
import com.example.models.hashedPassword
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll

suspend fun insertCustomer(customer: Customer) = dbQuery {
    CustomerTable.insert {
        it[id] = customer.id
        it[username] = customer.username
        it[firstName] = customer.firstName
        it[lastName] = customer.lastName
        it[gender] = customer.gender
        it[phone] = customer.phone
        it[email] = customer.email
        it[password] = hashedPassword(customer.password)
        it[status] = customer.status
    }
}

suspend fun signInCustomer(username: String, password: String): TokePair? = dbQuery {
    val customerInDb = CustomerTable.selectAll().where { CustomerTable.username eq username }
        .mapNotNull { resultRow(it) }
        .singleOrNull()

    if (customerInDb != null && customerInDb.password == hashedPassword(password)) {
        generateTokens(username, Config.SECRET, Config.ISSUER, Config.AUDIENCE)
    } else {
        null
    }
}

private fun resultRow(row: ResultRow) = Customer(
    row[CustomerTable.id],
    row[CustomerTable.username],
    row[CustomerTable.firstName],
    row[CustomerTable.lastName],
    row[CustomerTable.phone],
    row[CustomerTable.gender],
    row[CustomerTable.email],
    row[CustomerTable.password],
    row[CustomerTable.status]
)

suspend fun getCustomerByUsername(username: String): Customer? = dbQuery {
    CustomerTable.selectAll().where { CustomerTable.username eq username }
        .mapNotNull { resultRow(it) }
        .singleOrNull()
}

suspend fun signOutCustomer(token: String) = dbQuery {
    TokenBlackList.blacklistToken(token)
}
