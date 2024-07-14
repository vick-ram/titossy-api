package com.example.dao

import com.example.db.payment.SupplierPayment
import com.example.models.payment.PaymentUpdateStatus
import com.example.models.payment.SupplierPaymentRequest
import com.example.models.payment.SupplierPaymentResponse
import java.util.*

interface SupplierPaymentRepository {
    suspend fun createPayment(employeeId: UUID, payment: SupplierPaymentRequest): SupplierPaymentResponse
    suspend fun updateSupplierPayment(id: UUID, employeeId: UUID, payment: SupplierPaymentRequest): Boolean
    suspend fun updatePaymentStatus(paymentId: UUID, paymentStatus: PaymentUpdateStatus): Boolean
    suspend fun filteredSupplierPayment(filter: (SupplierPayment) -> Boolean): List<SupplierPaymentResponse>
    suspend fun deleteSupplierPayment(paymentId: UUID): Boolean
}