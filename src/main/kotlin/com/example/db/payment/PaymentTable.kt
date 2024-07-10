package com.example.db.payment

import com.example.db.booking.Booking
import com.example.db.booking.Bookings
import com.example.db.employee.Employee
import com.example.db.employee.EmployeeTable
import com.example.db.order.PurchaseOrder
import com.example.db.order.PurchaseOrders
import com.example.db.util.CustomUUIDEntity
import com.example.db.util.CustomUUIDEntityClass
import com.example.db.util.CustomUUIDTable
import com.example.db.util.PGEnum
import com.example.models.payment.CustomerPaymentResponse
import com.example.models.payment.PaymentMethod
import com.example.models.payment.PaymentStatus
import com.example.models.payment.SupplierPaymentResponse
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.ReferenceOption
import java.util.*

object CustomerPaymentTable : CustomUUIDTable("customer_payments") {
    val bookingId = reference("booking_id", Bookings, onDelete = ReferenceOption.CASCADE)
    val amount = decimal("amount", 10, 2)
    val paymentMethod = customEnumeration(
        "payment_method",
        "Paymentmethod",
        { value -> PaymentMethod.valueOf(value as String) },
        { PGEnum("PaymentMethod", it) }
    ).default(PaymentMethod.MOBILE).nullable()
    val phone = varchar("phone", 50)
    val transactionCode = varchar("transaction_code", 50).uniqueIndex()
    val paymentStatus = customEnumeration(
        "status",
        "Paymentstatus",
        { value -> PaymentStatus.valueOf(value as String) },
        { PGEnum("PaymentStatus", it) }
    ).default(PaymentStatus.PENDING)
}

class CustomerPayment(id: EntityID<UUID>) : CustomUUIDEntity(id, CustomerPaymentTable) {
    companion object : CustomUUIDEntityClass<CustomerPayment>(CustomerPaymentTable)

    var bookingId by Booking referencedOn CustomerPaymentTable.bookingId
    var amount by CustomerPaymentTable.amount
    var paymentMethod by CustomerPaymentTable.paymentMethod
    var phone by CustomerPaymentTable.phone
    var refNumber by CustomerPaymentTable.transactionCode
    var paymentStatus by CustomerPaymentTable.paymentStatus

    fun toCustomerPaymentResponse() = CustomerPaymentResponse(
        id.value,
        bookingId.id.value,
        amount,
        paymentMethod?.name,
        phone,
        refNumber,
        paymentStatus,
        createdAt,
        updatedAt
    )
}

object SupplierPaymentTable : CustomUUIDTable("supplier_payments") {
    val employee = reference("employee_id", EmployeeTable, onDelete = ReferenceOption.CASCADE)
    val orderId = reference("order_id", PurchaseOrders, onDelete = ReferenceOption.CASCADE)
    val amount = decimal("amount", 10, 2)
    val paymentMethod = customEnumeration(
        "payment_method",
        "Paymentmethod",
        { value -> PaymentMethod.valueOf(value as String) },
        { PGEnum("PaymentMethod", it) }
    )
    val paymentRefNumber = varchar("ref_number", 20)
    val paymentStatus = customEnumeration(
        "status",
        "Paymentstatus",
        { value -> PaymentStatus.valueOf(value as String) },
        { PGEnum("PaymentStatus", it) }
    )
}

class SupplierPayment(id: EntityID<UUID>) : CustomUUIDEntity(id, SupplierPaymentTable) {
    companion object : CustomUUIDEntityClass<SupplierPayment>(SupplierPaymentTable)

    var employee by Employee referencedOn SupplierPaymentTable.employee
    var orderId by PurchaseOrder referencedOn SupplierPaymentTable.orderId
    var amount by SupplierPaymentTable.amount
    var paymentMethod by SupplierPaymentTable.paymentMethod
    var paymentStatus by SupplierPaymentTable.paymentStatus
    var refNumber by SupplierPaymentTable.paymentRefNumber

    fun toSupplierPaymentResponse() = SupplierPaymentResponse(
        id.value,
        employee.id.value,
        orderId.id.value,
        createdAt,
        amount,
        paymentMethod,
        refNumber,
        paymentStatus,
        updatedAt
    )
}


