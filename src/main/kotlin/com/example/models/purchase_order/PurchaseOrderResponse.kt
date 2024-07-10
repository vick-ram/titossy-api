package com.example.models.purchase_order

import com.example.models.util.BigDecimalSerializer
import com.example.models.util.LocalDateSerializer
import com.example.models.util.LocalDateTimeSerializer
import kotlinx.serialization.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

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
    val orderStatus: OrderStatus,
    @Serializable(with = LocalDateTimeSerializer::class)
    val createdAt: LocalDateTime,
    @Serializable(with = LocalDateTimeSerializer::class)
    val updatedAt: LocalDateTime
)
