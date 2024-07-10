package com.example.models.purchase_order

import com.example.models.util.BigDecimalSerializer
import com.example.models.util.LocalDateTimeSerializer
import com.example.models.util.UUIDSerializer
import kotlinx.serialization.Serializable
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

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
