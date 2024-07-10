package com.example.models.cart

import com.example.models.util.UUIDSerializer
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class ProductCart(
    @Serializable(with = UUIDSerializer::class)
    val productId: UUID,
    val quantity: Int
)
