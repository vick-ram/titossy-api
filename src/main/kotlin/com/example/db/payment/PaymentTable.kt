package com.example.db.payment

import com.example.db.booking.Booking
import com.example.db.booking.BookingTable
import com.example.db.order.Order
import com.example.db.order.OrderTable
import com.example.db.util.PGEnum
import com.example.models.payment.PaymentStatus
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.javatime.datetime
import java.time.LocalDateTime

object CustomerPaymentTable: IntIdTable() {
    val bookingId = reference("booking_id", BookingTable, onDelete = ReferenceOption.CASCADE)
    val paymentDate = datetime("payment_date").clientDefault { LocalDateTime.now() }
    val amount = decimal("amount", 10, 2)
    val paymentMethod = varchar("method", 50)
    val refNumber = varchar("ref_number", 50).nullable()
    val paymentStatus = customEnumeration(
        "status",
        "Paymentstatus",
        { value -> PaymentStatus.valueOf(value as String) },
        { PGEnum("PaymentStatus", it) }
    )
}

class CustomerPayment(id: EntityID<Int>) : IntEntity(id) {
    companion object: IntEntityClass<CustomerPayment>(CustomerPaymentTable)

    var bookingId by Booking referencedOn CustomerPaymentTable.bookingId
    var paymentDate by CustomerPaymentTable.paymentDate
    var amount by CustomerPaymentTable.amount
    var paymentMethod by CustomerPaymentTable.paymentMethod
    var refNumber by CustomerPaymentTable.refNumber
    var paymentStatus by CustomerPaymentTable.paymentStatus
}

object SupplierPaymentTable: IntIdTable() {
    val orderId = reference("order_id", OrderTable, onDelete = ReferenceOption.CASCADE)
    val paymentDate = datetime("payment_date")
    val amount = decimal("amount", 10, 2)
    val paymentMethod = varchar("method", 50)
    val paymentStatus = customEnumeration(
        "status",
        "Paymentstatus",
        { value -> PaymentStatus.valueOf(value as String) },
        { PGEnum("PaymentStatus", it) }
    )
}

class SupplierPayment(id: EntityID<Int>) : IntEntity(id) {
    companion object: IntEntityClass<SupplierPayment>(SupplierPaymentTable)

    var orderId by Order referencedOn SupplierPaymentTable.orderId
    var paymentDate by SupplierPaymentTable.paymentDate
    var amount by SupplierPaymentTable.amount
    var paymentMethod by SupplierPaymentTable.paymentMethod
    var paymentStatus by SupplierPaymentTable.paymentStatus
}
