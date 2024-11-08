package com.example.db

import com.example.commons.*
import com.example.models.CustomerPaymentResponse
import com.example.models.SupplierPaymentResponse
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.ReferenceOption

object CustomerPaymentTable : StringUUIDTable("customer_payments") {
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

class CustomerPayment(id: EntityID<String>) : StringUUIDEntity(id, CustomerPaymentTable) {
    companion object : StringUUIDEntityClass<CustomerPayment>(CustomerPaymentTable)

    var bookingId by Booking referencedOn CustomerPaymentTable.bookingId
    var amount by CustomerPaymentTable.amount
    var paymentMethod by CustomerPaymentTable.paymentMethod
    var phone by CustomerPaymentTable.phone
    var refNumber by CustomerPaymentTable.transactionCode
    var paymentStatus by CustomerPaymentTable.paymentStatus

    private val bookingItems by lazy { bookingId.bookingServiceAddOns }

    fun toCustomerPaymentResponse() = CustomerPaymentResponse(
        id.value,
        bookingId.id.value,
        bookingItems.map { it.toBookingServiceAddOnResponse() },
        amount,
        paymentMethod?.name,
        phone,
        refNumber,
        paymentStatus,
        createdAt,
        updatedAt
    )
}

object SupplierPaymentTable : StringUUIDTable("supplier_payments") {
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

class SupplierPayment(id: EntityID<String>) : StringUUIDEntity(id, SupplierPaymentTable) {
    companion object : StringUUIDEntityClass<SupplierPayment>(SupplierPaymentTable)

    var employee by Employee referencedOn SupplierPaymentTable.employee
    var orderId by PurchaseOrder referencedOn SupplierPaymentTable.orderId
    var amount by SupplierPaymentTable.amount
    var paymentMethod by SupplierPaymentTable.paymentMethod
    var paymentStatus by SupplierPaymentTable.paymentStatus
    var refNumber by SupplierPaymentTable.paymentRefNumber

    fun toSupplierPaymentResponse() = SupplierPaymentResponse(
        paymentId = id.value,
        employee = employee.fullName,
        orderId = orderId.id.value,
        supplier = orderId.supplier.fullName,
        paymentDate = createdAt,
        amount = amount,
        method = paymentMethod,
        paymentReference = refNumber,
        status = paymentStatus,
        updatedAt = updatedAt
    )
}


