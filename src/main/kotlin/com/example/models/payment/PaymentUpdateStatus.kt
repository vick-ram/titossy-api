package com.example.models.payment

import kotlinx.serialization.Serializable
import org.valiktor.functions.isIn
import org.valiktor.validate

@Serializable
data class PaymentUpdateStatus(
    val status: PaymentStatus
) {
    fun validate(): PaymentUpdateStatus {
        validate(this) {
            validate(PaymentUpdateStatus::status).isIn(PaymentStatus.entries)
        }
        return this
    }
}