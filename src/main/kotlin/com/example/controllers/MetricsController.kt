package com.example.controllers

import com.example.commons.Availability
import com.example.commons.DatabaseUtil.dbQuery
import com.example.db.Bookings
import com.example.db.CustomerTable
import com.example.db.EmployeeTable
import com.example.db.SupplierTable
import com.example.commons.Roles
import com.example.db.CustomerPaymentTable
import com.example.db.SupplierPaymentTable
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SqlExpressionBuilder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.slice
import org.jetbrains.exposed.sql.sum
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime

@Serializable
data class BookingMetrics(
    val total: Int,
    val daily: Int,
    val weekly: Int,
    val monthly: Int,
    val yearly: Int
)

@Serializable
data class PaymentMetrics(
    val customerPayments: Double,
    val supplierPayments: Double,
    val net: Double
)

@Serializable
data class UserRoleBreakdown(
    val manager: Int,
    val supervisor: Int,
    val finance: Int,
    val cleaner: Int,
    val inventoryManager: Int
)

@Serializable
data class UserMetrics(
    val employees: Int,
    val customers: Int,
    val suppliers: Int,
    val activeCleaners: Int,
    val roles: UserRoleBreakdown
)

@Serializable
data class Metrics(
    val bookings: BookingMetrics,
    val payments: PaymentMetrics,
    val users: UserMetrics
)

private fun getBookingMetrics(): BookingMetrics? = transaction {
    val now = LocalDateTime.now()
    val today = now.toLocalDate()
    val startOfWeek = today.with(java.time.DayOfWeek.MONDAY)
    val startOfMonth = today.withDayOfMonth(1)
    val startOfYear = today.withDayOfYear(1)
    val total = Bookings.selectAll().count()
    val daily = Bookings.selectAll()
        .where { Bookings.bookingDateTime.between(today.atStartOfDay(), today.plusDays(1).atStartOfDay()) }
        .count()
    val weekly = Bookings.selectAll()
        .where { Bookings.bookingDateTime.between(startOfWeek.atStartOfDay(), today.plusDays(1).atStartOfDay()) }
        .count()
    val monthly = Bookings.selectAll()
        .where { Bookings.bookingDateTime.between(startOfMonth.atStartOfDay(), today.plusDays(1).atStartOfDay()) }
        .count()
    val yearly = Bookings.selectAll()
        .where { Bookings.bookingDateTime.between(startOfYear.atStartOfDay(), today.plusDays(1).atStartOfDay()) }
        .count()
    return@transaction BookingMetrics(
        total = total.toInt(),
        daily = daily.toInt(),
        weekly = weekly.toInt(),
        monthly = monthly.toInt(),
        yearly = yearly.toInt()
    )
}

fun getPaymentMetrics(): PaymentMetrics? = transaction {
    val customerPayments = CustomerPaymentTable
        .select(CustomerPaymentTable.amount.sum())
        .single()[CustomerPaymentTable.amount.sum()] ?: 0.0.toBigDecimal()

    val supplierPayments = SupplierPaymentTable
        .select(SupplierPaymentTable.amount.sum())
        .single()[SupplierPaymentTable.amount.sum()] ?: 0.0.toBigDecimal()

    val net = customerPayments - supplierPayments

    return@transaction PaymentMetrics(
        customerPayments = customerPayments.toDouble(),
        supplierPayments = supplierPayments.toDouble(),
        net = net.toDouble()
    )
}

fun getUserMetrics(): UserMetrics? = transaction {
    val employees = EmployeeTable.selectAll().count()
    val customers = CustomerTable.selectAll().count()
    val suppliers = SupplierTable.selectAll().count()

    val manager = EmployeeTable.selectAll().where { EmployeeTable.role eq Roles.MANAGER }.count()
    val supervisor = EmployeeTable.selectAll().where { EmployeeTable.role eq Roles.SUPERVISOR }.count()
    val finance = EmployeeTable.selectAll().where { EmployeeTable.role eq Roles.FINANCE }.count()
    val cleaner = EmployeeTable.selectAll().where { EmployeeTable.role eq Roles.CLEANER }.count()
    val inventoryManager = EmployeeTable.selectAll().where { EmployeeTable.role eq Roles.INVENTORY }.count()

    val activeCleaners = EmployeeTable.selectAll().where {
        (EmployeeTable.role eq Roles.CLEANER) and
                (EmployeeTable.availability eq Availability.AVAILABLE)
    }.count()

    return@transaction UserMetrics(
        employees = employees.toInt(),
        customers = customers.toInt(),
        suppliers = suppliers.toInt(),
        activeCleaners = activeCleaners.toInt(),
        roles = UserRoleBreakdown(
            manager = manager.toInt(),
            supervisor = supervisor.toInt(),
            finance = finance.toInt(),
            cleaner = cleaner.toInt(),
            inventoryManager = inventoryManager.toInt()
        )
    )
}

public suspend fun getMetrics(): Metrics? = dbQuery {
    val bookingsMetrics = getBookingMetrics()
    val paymentMetrics = getPaymentMetrics()
    val userMetrics = getUserMetrics()

    return@dbQuery Metrics(
        bookings = bookingsMetrics!!,
        payments = paymentMetrics!!,
        users = userMetrics!!,
    )
}
