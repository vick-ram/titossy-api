package com.example.dao

import com.example.db.payment.CustomerPayment
import com.example.models.payment.CustomerPaymentRequest
import com.example.models.payment.CustomerPaymentResponse
import com.example.models.payment.PaymentUpdateStatus
import java.util.*

interface CustomerPaymentRepository {
    suspend fun createPayment(payment: CustomerPaymentRequest): CustomerPaymentResponse
    suspend fun updateCustomerPayment(id: UUID, payment: CustomerPaymentRequest): Boolean
    suspend fun updatePaymentStatus(paymentId: UUID, paymentStatus: PaymentUpdateStatus): Boolean
    suspend fun filteredCustomerPayment(filter: (CustomerPayment) -> Boolean): List<CustomerPaymentResponse>
    suspend fun deleteCustomerPayment(paymentId: UUID): Boolean
}