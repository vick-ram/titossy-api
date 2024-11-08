package com.example.dao

import com.example.db.CustomerPayment
import com.example.models.CustomerPaymentRequest
import com.example.models.CustomerPaymentResponse
import com.example.models.PaymentUpdateStatus

interface CustomerPaymentRepository {
    suspend fun createPayment(payment: CustomerPaymentRequest): CustomerPaymentResponse
    suspend fun updateCustomerPayment(id: String, payment: CustomerPaymentRequest): Boolean
    suspend fun updatePaymentStatus(paymentId: String, paymentStatus: PaymentUpdateStatus): Boolean
    suspend fun filteredCustomerPayment(filter: (CustomerPayment) -> Boolean): List<CustomerPaymentResponse>
    suspend fun deleteCustomerPayment(paymentId: String): Boolean
}