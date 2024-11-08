package com.example.models

import com.example.commons.*
import kotlinx.serialization.Serializable
import org.valiktor.functions.isNotNull
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

@Serializable
data class PurchaseOrderItemRequest(
    @Serializable(with = UUIDSerializer::class)
    val productId: UUID,
    val quantity: Int,
    @Serializable(with = BigDecimalSerializer::class)
    val unitPrice: BigDecimal
)

@Serializable
data class PurchaseOrderItemResponse(
    val purchaseOrderItemId: String,
    val product: String,
    val quantity: Int,
    @Serializable(with = BigDecimalSerializer::class)
    val unitPrice: BigDecimal,
    @Serializable(with = BigDecimalSerializer::class)
    val subtotal: BigDecimal
)

@Serializable
data class PurchaseOrderRequest(
    val supplierId: String,
    @Serializable(with = LocalDateSerializer::class)
    val expectedDate: LocalDate
) {
    fun validate(): PurchaseOrderRequest {
        org.valiktor.validate(this) {
            validate(PurchaseOrderRequest::supplierId).isNotNull()
        }
        return this
    }
}

@Serializable
data class UpdateOrderStatus(
    val status: OrderStatus
)

@Serializable
data class PurchaseOrderResponse(
    val purchaseOrderId: String,
    val employee: String,
    val supplier: String,
    @Serializable(with = LocalDateSerializer::class)
    val expectedDate: LocalDate,
    val purchaseOrderItems: MutableList<PurchaseOrderItemResponse>? = null,
    @Serializable(with = BigDecimalSerializer::class)
    val totalAmount: BigDecimal,
    val paid: Boolean,
    val orderStatus: OrderStatus,
    @Serializable(with = LocalDateTimeSerializer::class)
    val createdAt: LocalDateTime,
    @Serializable(with = LocalDateTimeSerializer::class)
    val updatedAt: LocalDateTime
)