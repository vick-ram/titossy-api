package com.example.controllers

import com.example.commons.DatabaseUtil.dbQuery
import com.example.commons.OrderStatus
import com.example.commons.PaymentMethod
import com.example.commons.PaymentStatus
import com.example.commons.generateRandomString
import com.example.dao.CustomerPaymentRepository
import com.example.dao.SupplierPaymentRepository
import com.example.db.*
import com.example.models.*
import io.ktor.server.plugins.*
import org.jetbrains.exposed.sql.selectAll
import java.math.BigDecimal

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
            NotificationEntity.new {
                this.booking = booking
                this.message = "Payment for booking ${booking.id} made successfully"
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
        id: String,
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
        paymentId: String,
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

    override suspend fun deleteCustomerPayment(paymentId: String): Boolean = dbQuery {
        val paymentExists = CustomerPaymentTable.selectAll()
            .where { CustomerPaymentTable.id eq paymentId }.count() > 0
        if (!paymentExists) {
            throw IllegalArgumentException("Payment with id $paymentId does not exist")
        }
        CustomerPayment.findById(paymentId)?.delete()
        return@dbQuery true
    }
}



class SupplierPaymentRepositoryImpl : SupplierPaymentRepository {
    override suspend fun createPayment(
        employeeId: String,
        payment: SupplierPaymentRequest
    ): SupplierPaymentResponse = dbQuery {
        val order = PurchaseOrder.findById(payment.orderId)
            ?: throw NotFoundException("Order with id ${payment.orderId} does not exist")
        val amount = order.totalAmount
        val supplierPayment = SupplierPayment.new {
            this.employee = Employee[employeeId]
            this.orderId = order
            this.amount = amount
            this.paymentMethod = payment.method
            this.refNumber = generateRandomString(4).prependIndent("TR-")
            this.paymentStatus = PaymentStatus.CONFIRMED
        }
        order.paid = true
        order.orderStatus = OrderStatus.COMPLETED
        return@dbQuery supplierPayment.toSupplierPaymentResponse()
    }

    override suspend fun updateSupplierPayment(
        id: String,
        employeeId: String,
        payment: SupplierPaymentRequest
    ): Boolean = dbQuery {
        val order = PurchaseOrder.findById(payment.orderId)
            ?: throw NotFoundException("Order with id ${payment.orderId} does not exist")
        val paymentExists = SupplierPaymentTable
            .selectAll()
            .where { SupplierPaymentTable.id eq id }
            .count() > 0
        if (!paymentExists) {
            throw IllegalArgumentException("payment with id $id does not exist")
        }
        SupplierPayment.findByIdAndUpdate(id) { update ->
            update.employee = Employee[employeeId]
            update.orderId = order
            update.amount = order.totalAmount
            update.paymentMethod = payment.method
            update.refNumber = generateRandomString(15)
            update.paymentStatus = PaymentStatus.CONFIRMED
        }
        return@dbQuery true
    }

    override suspend fun updatePaymentStatus(
        paymentId: String,
        paymentStatus: PaymentUpdateStatus
    ): Boolean = dbQuery {
        SupplierPayment.findByIdAndUpdate(paymentId) { statusUpdate ->
            statusUpdate.paymentStatus = paymentStatus.status
        }
        return@dbQuery true
    }

    override suspend fun filteredSupplierPayment(
        filter: (SupplierPayment) -> Boolean
    ): List<SupplierPaymentResponse> =
        dbQuery {
            return@dbQuery SupplierPayment.all()
                .filter(filter)
                .sortedByDescending { it.createdAt.coerceAtLeast(it.updatedAt) }
                .map { it.toSupplierPaymentResponse() }
        }

    override suspend fun deleteSupplierPayment(paymentId: String): Boolean = dbQuery {
        val paymentExists = SupplierPaymentTable.selectAll()
            .where { SupplierPaymentTable.id eq paymentId }.count() > 0
        if (!paymentExists) {
            throw IllegalArgumentException(
                "Payment with id $paymentId does not exist"
            )
        }
        SupplierPayment.findById(paymentId)?.delete()
        return@dbQuery true
    }
}