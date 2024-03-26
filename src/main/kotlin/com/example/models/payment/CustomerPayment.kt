package com.example.models.payment

import com.example.models.util.DateSerializer
import kotlinx.serialization.Serializable
import org.valiktor.functions.hasSize
import org.valiktor.functions.isEqualToIgnoringCase
import org.valiktor.functions.isNotBlank
import org.valiktor.functions.isPositive
import org.valiktor.validate
import java.util.Date

@Serializable
data class CustomerPaymentRequest(
    val bookingId: Int,
    val amount: Double,
    val method: String,
    val refNumber: String?
) {
    init {
        validate(this) {
            validate(CustomerPaymentRequest::amount).isPositive()
            validate(CustomerPaymentRequest::method).isNotBlank()
            refNumber?.let { it1 ->
                validate(CustomerPaymentRequest::refNumber).hasSize(10).isEqualToIgnoringCase(it1.uppercase())
            }
        }

    }
}

@Serializable
data class CustomerPaymentResponse(
    val id: Int,
    val bookingId: Int,
    @Serializable(with = DateSerializer::class)
    val date: Date,
    val amount: Double,
    val method: String,
    val refNumber: String?,
    val status: PaymentStatus
)