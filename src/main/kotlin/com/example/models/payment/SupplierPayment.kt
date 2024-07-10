package com.example.models.payment

import com.example.models.util.BigDecimalSerializer
import com.example.models.util.LocalDateTimeSerializer
import com.example.models.util.UUIDSerializer
import kotlinx.serialization.Serializable
import org.valiktor.functions.isIn
import org.valiktor.functions.isNotNull
import org.valiktor.validate
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

@Serializable
data class SupplierPaymentRequest(
    @Serializable(with = UUIDSerializer::class)
    val employeeId: UUID,
    val orderId: String,
    val method: PaymentMethod
) {
    fun validate(): SupplierPaymentRequest {
        validate(this) {
            validate(SupplierPaymentRequest::orderId).isNotNull()
            validate(SupplierPaymentRequest::method).isIn(*PaymentMethod::class.java.enumConstants)
        }
        return this
    }
}

@Serializable
data class SupplierPaymentResponse(
    @Serializable(with = UUIDSerializer::class)
    val supplierPaymentID: UUID,
    @Serializable(with = UUIDSerializer::class)
    val employeeId: UUID,
    val orderId: String,
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