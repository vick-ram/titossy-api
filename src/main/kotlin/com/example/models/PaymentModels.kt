package com.example.models

import com.example.commons.BigDecimalSerializer
import com.example.commons.LocalDateTimeSerializer
import com.example.commons.PaymentMethod
import com.example.commons.PaymentStatus
import com.example.commons.isPhoneNumber
import com.example.commons.withMessage
import kotlinx.serialization.Serializable
import org.valiktor.functions.*
import java.math.BigDecimal
import java.time.LocalDateTime

@Serializable
data class CustomerPaymentRequest(
    val bookingId: String,
    val phoneNumber: String,
    val transactionCode: String
) {
    fun validate(): CustomerPaymentRequest {
        org.valiktor.validate(this) {
            validate(CustomerPaymentRequest::transactionCode)
                .isNotEmpty().withMessage("Transaction code cannot be empty")
                .hasSize(10).withMessage("Transaction code must be 10 characters long")
                .matches(Regex("[A-Z0-9]+"))
                .withMessage("Transaction code must contain only uppercase letters and digits")
                .matches(Regex(".*\\d.*")).withMessage("Transaction code must contain at least one digit")
            validate(CustomerPaymentRequest::phoneNumber)
                .isPhoneNumber()
        }
        return this
    }
}

@Serializable
data class PaymentUpdateStatus(
    val status: PaymentStatus
) {
    fun validate(): PaymentUpdateStatus {
        org.valiktor.validate(this) {
            validate(PaymentUpdateStatus::status).isIn(PaymentStatus.entries)
        }
        return this
    }
}

@Serializable
data class CustomerPaymentResponse(
    val paymentId: String,
    val bookingId: String,
    val bookingItems: List<BookingServiceAddOnResponse> = emptyList(),
    @Serializable(with = BigDecimalSerializer::class)
    val amount: BigDecimal,
    val paymentMethod: String?,
    val phoneNumber: String,
    val transactionCode: String,
    val paymentStatus: PaymentStatus,
    @Serializable(with = LocalDateTimeSerializer::class)
    val createdAt: LocalDateTime,
    @Serializable(with = LocalDateTimeSerializer::class)
    val updatedAt: LocalDateTime,
)

@Serializable
data class SupplierPaymentRequest(
    val orderId: String,
    val method: PaymentMethod
) {
    fun validate(): SupplierPaymentRequest {
        org.valiktor.validate(this) {
            validate(SupplierPaymentRequest::orderId).isNotNull()
            validate(SupplierPaymentRequest::method).isIn(PaymentMethod.entries)
        }
        return this
    }
}

@Serializable
data class SupplierPaymentResponse(
    val paymentId: String,
    val employee: String,
    val orderId: String,
    val supplier: String,
    @Serializable(with = LocalDateTimeSerializer::class)
    val paymentDate: LocalDateTime,
    @Serializable(with = BigDecimalSerializer::class)
    val amount: BigDecimal,
    val method: PaymentMethod,
    val paymentReference: String,
    val status: PaymentStatus,
    @Serializable(with = LocalDateTimeSerializer::class)
    val updatedAt: LocalDateTime
)