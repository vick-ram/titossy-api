package com.example.models.payment

import com.example.exceptions.isPhoneNumber
import com.example.exceptions.withMessage
import com.example.models.booking.BookingResponse
import com.example.models.util.BigDecimalSerializer
import com.example.models.util.LocalDateTimeSerializer
import com.example.models.util.UUIDSerializer
import io.ktor.util.reflect.*
import kotlinx.serialization.Serializable
import org.valiktor.functions.*
import org.valiktor.validate
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

@Serializable
data class CustomerPaymentRequest(
    val bookingId: String,
    val phoneNumber: String,
    val transactionCode: String
) {
    fun validate(): CustomerPaymentRequest {
        validate(this) {
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
data class CustomerPaymentResponse(
    @Serializable(with = UUIDSerializer::class)
    val paymentId: UUID,
    val bookingId: String,
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

enum class PaymentMethod {
    CASH, MOBILE, CARD
}