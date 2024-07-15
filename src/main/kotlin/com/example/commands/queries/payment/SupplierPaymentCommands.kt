package com.example.commands.queries.payment

import com.example.dao.SupplierPaymentRepository
import com.example.db.employee.Employee
import com.example.db.order.PurchaseOrder
import com.example.db.payment.SupplierPayment
import com.example.db.payment.SupplierPaymentTable
import com.example.db.util.DatabaseUtil.dbQuery
import com.example.models.payment.PaymentStatus
import com.example.models.payment.PaymentUpdateStatus
import com.example.models.payment.SupplierPaymentRequest
import com.example.models.payment.SupplierPaymentResponse
import com.example.models.purchase_order.OrderStatus
import com.example.models.util.generateRandomString
import io.ktor.server.plugins.*
import org.jetbrains.exposed.sql.selectAll
import java.util.*

class SupplierPaymentRepositoryImpl : SupplierPaymentRepository {
    override suspend fun createPayment(
        employeeId: UUID,
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
        id: UUID,
        employeeId: UUID,
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
        paymentId: UUID,
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

    override suspend fun deleteSupplierPayment(paymentId: UUID): Boolean = dbQuery {
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