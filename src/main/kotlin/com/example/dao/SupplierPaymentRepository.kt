package com.example.dao

import com.example.db.SupplierPayment
import com.example.models.PaymentUpdateStatus
import com.example.models.SupplierPaymentRequest
import com.example.models.SupplierPaymentResponse

interface SupplierPaymentRepository {
    suspend fun createPayment(employeeId: String, payment: SupplierPaymentRequest): SupplierPaymentResponse
    suspend fun updateSupplierPayment(id: String, employeeId: String, payment: SupplierPaymentRequest): Boolean
    suspend fun updatePaymentStatus(paymentId: String, paymentStatus: PaymentUpdateStatus): Boolean
    suspend fun filteredSupplierPayment(filter: (SupplierPayment) -> Boolean): List<SupplierPaymentResponse>
    suspend fun deleteSupplierPayment(paymentId: String): Boolean
}