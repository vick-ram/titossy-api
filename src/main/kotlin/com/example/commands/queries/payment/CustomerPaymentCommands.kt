package com.example.commands.queries.payment

import com.example.db.booking.Booking
import com.example.db.payment.CustomerPayment
import com.example.db.payment.CustomerPaymentTable
import com.example.db.util.DatabaseUtil.dbQuery
import com.example.models.payment.CustomerPaymentRequest
import com.example.models.payment.CustomerPaymentResponse
import com.example.models.payment.PaymentStatus
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

private fun CustomerPayment.toCustomerPaymentResponse() = CustomerPaymentResponse(
    id = this.id.value,
    bookingId = this.bookingId.id.value,
    date = Date.from(this.paymentDate.atZone(ZoneId.systemDefault()).toInstant()),
    amount = this.amount.toDouble(),
    method = this.paymentMethod,
    refNumber = this.refNumber,
    status = this.paymentStatus
)

suspend fun makePayment(paymentRequest: CustomerPaymentRequest): CustomerPaymentResponse? = dbQuery {
    return@dbQuery try {
        CustomerPayment.new {
            this.bookingId = Booking[paymentRequest.bookingId]
            this.paymentDate = LocalDateTime.now()
            this.amount = paymentRequest.amount.toBigDecimal()
            this.paymentMethod = paymentRequest.method
            this.refNumber = paymentRequest.refNumber
            this.paymentStatus = PaymentStatus.PENDING
        }.toCustomerPaymentResponse()
    }catch (e: Exception){
        null
    }
}

suspend fun getCustomerPayment(paymentId: Int): CustomerPaymentResponse? = dbQuery {
    return@dbQuery try {
        CustomerPayment.findById(paymentId)?.toCustomerPaymentResponse()
    }catch (e: Exception){
        null
    }
}

suspend fun getPaymentsByBooking(bookingId: Int): List<CustomerPaymentResponse> = dbQuery {
    return@dbQuery try {
        CustomerPayment.find { CustomerPaymentTable.bookingId eq bookingId }.map { it.toCustomerPaymentResponse() }
    }catch (e: Exception){
        emptyList()
    }
}

suspend fun updateCustomerPaymentStatus(paymentId: Int, status: PaymentStatus): CustomerPaymentResponse? = dbQuery {
    return@dbQuery try {
        val payment = CustomerPayment.findById(paymentId)
        payment?.apply {
            this.paymentStatus = status
        }?.toCustomerPaymentResponse() ?: run {
            null
        }
    }catch (e: Exception){
        null
    }
}

suspend fun deletePayment(paymentId: Int): Boolean = dbQuery {
    return@dbQuery try {
        val payment = CustomerPayment.findById(paymentId) ?: return@dbQuery false
        payment.delete()
        true
    }catch (e: Exception){
        false
    }
}
