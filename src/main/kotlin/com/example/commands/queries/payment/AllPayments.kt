package com.example.commands.queries.payment

import com.example.db.payment.CustomerPaymentTable
import com.example.db.payment.SupplierPaymentTable
import com.example.db.util.DatabaseUtil.dbQuery
import org.jetbrains.exposed.sql.selectAll

suspend fun netPayments() = dbQuery {
    val customerPaymentTotal = CustomerPaymentTable
        .selectAll()
        .sumOf { it[CustomerPaymentTable.amount].toDouble() }
    val supplierPaymentTotal = SupplierPaymentTable
        .selectAll()
        .sumOf { it[SupplierPaymentTable.amount].toDouble() }
    customerPaymentTotal - supplierPaymentTotal
}