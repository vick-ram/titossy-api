package com.example.models.product

import com.example.models.util.BigDecimalSerializer
import com.example.models.util.LocalDateTimeSerializer
import com.example.models.util.UUIDSerializer
import kotlinx.serialization.Serializable
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

@Serializable
data class ProductResponse(
    @Serializable(with = UUIDSerializer::class)
    val productId: UUID,
    val name: String,
    val description: String,
    @Serializable(with = BigDecimalSerializer::class)
    val unitPrice: BigDecimal,
    val image: String?,
    val stock: Int,
    val reorderLevel: Int,
    val sku: String,
    @Serializable(with = LocalDateTimeSerializer::class)
    val createdAt: LocalDateTime,
    @Serializable(with = LocalDateTimeSerializer::class)
    val updatedAt: LocalDateTime
) : java.io.Serializable
