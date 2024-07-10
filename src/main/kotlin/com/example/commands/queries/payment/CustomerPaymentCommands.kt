package com.example.commands.queries.payment

import com.example.dao.CustomerPaymentRepository
import com.example.db.booking.Booking
import com.example.db.payment.CustomerPayment
import com.example.db.payment.CustomerPaymentTable
import com.example.db.util.DatabaseUtil.dbQuery
import com.example.models.payment.*
import io.ktor.server.plugins.*
import org.jetbrains.exposed.sql.selectAll
import java.math.BigDecimal
import java.util.*

class CustomerPaymentRepositoryImpl : CustomerPaymentRepository {
    override suspend fun createPayment(payment: CustomerPaymentRequest): CustomerPaymentResponse = dbQuery {
        try {
            val paymentExists = CustomerPaymentTable.selectAll()
                .where { CustomerPaymentTable.transactionCode eq payment.transactionCode }.count() > 0
            if (paymentExists) {
                throw IllegalArgumentException("Payment with refNumber ${payment.transactionCode} already exists")
            }
            val booking = Booking.findById(payment.bookingId)
                ?: throw NotFoundException("Booking with id ${payment.bookingId} does not exist")
            val customerPayment = CustomerPayment.new {
                this.bookingId = booking
                this.amount = booking.totalAmount
                this.paymentMethod = PaymentMethod.MOBILE
                this.phone = payment.phoneNumber
                this.refNumber = payment.transactionCode
                this.paymentStatus = PaymentStatus.PENDING
            }
            booking.paid = true
            return@dbQuery customerPayment.toCustomerPaymentResponse()
        } catch (e: Exception) {
            when (e) {
                is IllegalArgumentException -> throw e
                is NotFoundException -> throw e
                else -> throw e
            }
        }
    }

    override suspend fun updateCustomerPayment(
        id: UUID,
        payment: CustomerPaymentRequest
    ): Boolean = dbQuery {
        val booking = Booking.findById(payment.bookingId)
            ?: throw NotFoundException("Booking with id ${payment.bookingId} does not exist")
        val paymentExists = CustomerPaymentTable
            .selectAll()
            .where { CustomerPaymentTable.id eq id }
            .count() > 0
        if (!paymentExists) {
            throw IllegalArgumentException("payment with id $id does not exist")
        }
        CustomerPayment.findByIdAndUpdate(id) { update ->
            update.bookingId = booking
            update.amount = BigDecimal.ZERO
            update.paymentMethod = PaymentMethod.MOBILE
            update.phone = payment.phoneNumber
            update.refNumber = payment.transactionCode
        }
        return@dbQuery true
    }

    override suspend fun updatePaymentStatus(
        paymentId: UUID,
        paymentStatus: PaymentUpdateStatus
    ): Boolean = dbQuery {
        val payment =
            CustomerPayment.findById(paymentId)
                ?: throw NotFoundException("Payment with id $paymentId does not exist")
        val booking = Booking.findById(payment.bookingId.id)
            ?: throw NotFoundException("Booking with id ${payment.bookingId} does not exist")
        when (paymentStatus.status) {
            PaymentStatus.PENDING -> {
                payment.amount = BigDecimal.ZERO
            }

            PaymentStatus.CONFIRMED -> {
                payment.amount += booking.totalAmount
            }

            PaymentStatus.REFUNDED -> {
                payment.amount = BigDecimal.ZERO
            }

            PaymentStatus.CANCELLED -> {
                payment.amount = BigDecimal.ZERO
            }
        }
        payment.paymentStatus = paymentStatus.status
        return@dbQuery true
    }

    override suspend fun filteredCustomerPayment(
        filter: (CustomerPayment) -> Boolean
    ): List<CustomerPaymentResponse> =
        dbQuery {
            return@dbQuery CustomerPayment.all()
                .filter(filter)
                .sortedByDescending { it.createdAt.coerceAtLeast(it.updatedAt) }
                .map { it.toCustomerPaymentResponse() }
        }

    override suspend fun deleteCustomerPayment(paymentId: UUID): Boolean = dbQuery {
        val paymentExists = CustomerPaymentTable.selectAll()
            .where { CustomerPaymentTable.id eq paymentId }.count() > 0
        if (!paymentExists) {
            throw IllegalArgumentException("Payment with id $paymentId does not exist")
        }
        CustomerPayment.findById(paymentId)?.delete()
        return@dbQuery true
    }
}
