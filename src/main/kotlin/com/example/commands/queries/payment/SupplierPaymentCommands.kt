package com.example.commands.queries.payment

import com.example.db.order.Order
import com.example.db.payment.SupplierPayment
import com.example.db.payment.SupplierPaymentTable
import com.example.db.util.DatabaseUtil.dbQuery
import com.example.models.payment.PaymentStatus
import com.example.models.payment.SupplierPaymentRequest
import com.example.models.payment.SupplierPaymentResponse
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

private fun SupplierPayment.toSupplierPaymentResponse() = SupplierPaymentResponse(
    id = this.id.value,
    orderId = this.orderId.id.value,
    date = Date.from(this.paymentDate.atZone(ZoneId.systemDefault()).toInstant()),
    amount = this.amount.toDouble(),
    method = this.paymentMethod,
    status = this.paymentStatus
)

suspend fun makePayment(paymentRequest: SupplierPaymentRequest): SupplierPaymentResponse? = dbQuery {
    return@dbQuery try {
        SupplierPayment.new {
            this.orderId = Order[paymentRequest.orderId]
            this.paymentDate = LocalDateTime.now()
            this.amount = paymentRequest.amount.toBigDecimal()
            this.paymentMethod = paymentRequest.method
            this.paymentStatus = PaymentStatus.PENDING
        }.toSupplierPaymentResponse()
    } catch (e: Exception) {
        null
    }
}

suspend fun getSupplierPayment(paymentId: Int): SupplierPaymentResponse? = dbQuery {
    return@dbQuery try {
        SupplierPayment.findById(paymentId)?.toSupplierPaymentResponse()
    } catch (e: Exception) {
        null
    }
}

suspend fun getPaymentsByOrder(orderId: Int): List<SupplierPaymentResponse> = dbQuery {
    return@dbQuery try {
        SupplierPayment.find { SupplierPaymentTable.orderId eq orderId }.map { it.toSupplierPaymentResponse() }
    } catch (e: Exception) {
        emptyList()
    }
}

suspend fun updateSupplierPaymentStatus(paymentId: Int, status: PaymentStatus): SupplierPaymentResponse? = dbQuery {
    return@dbQuery try {
        val payment = SupplierPayment.findById(paymentId)
        payment?.apply {
            this.paymentStatus = status
        }?.toSupplierPaymentResponse() ?: run {
            null
        }
    } catch (e: Exception) {
        null
    }
}

suspend fun deleteSupplierPayment(paymentId: Int): Boolean = dbQuery {
    return@dbQuery try {
        SupplierPayment.findById(paymentId)?.delete()
        true
    } catch (e: Exception) {
        false
    }
}