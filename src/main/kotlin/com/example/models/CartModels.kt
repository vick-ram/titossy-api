package com.example.models

import com.example.commons.BigDecimalSerializer
import com.example.commons.UUIDSerializer
import kotlinx.serialization.Serializable
import java.math.BigDecimal
import java.util.*

@Serializable
data class ProductCart(
    @Serializable(with = UUIDSerializer::class)
    val productId: UUID,
    val quantity: Int
)

@Serializable
data class ServiceCartResponse(
    val customerId: String,
    val service: ServiceCartItemResponse? = null,
    val addOns: ServiceAddOnCartItemResponse? = null,
    @Serializable(with = BigDecimalSerializer::class)
    val total: BigDecimal
)

@Serializable
data class ServiceCartItemResponse(
    @Serializable(with = UUIDSerializer::class)
    val id: UUID,
    val name: String,
    @Serializable(with = BigDecimalSerializer::class)
    val price: BigDecimal,
    val thumbnail: String?,
    val quantity: Int
)

@Serializable
data class ServiceAddOnCartItemResponse(
    @Serializable(with = UUIDSerializer::class)
    val id: UUID,
    val name: String,
    @Serializable(with = BigDecimalSerializer::class)
    val price: BigDecimal,
    val thumbnail: String?,
    val quantity: Int
)

@Serializable
data class AddServiceToCart(
    @Serializable(with = UUIDSerializer::class)
    val serviceId: UUID? = null,
    val quantity: Int = 1
)

@Serializable
data class AddServiceAddonToCart(
    @Serializable(with = UUIDSerializer::class)
    val serviceAddon: UUID,
    val quantity: Int = 1
)