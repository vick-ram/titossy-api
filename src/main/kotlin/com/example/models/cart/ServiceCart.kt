package com.example.models.cart

import com.example.models.util.BigDecimalSerializer
import com.example.models.util.UUIDSerializer
import kotlinx.serialization.Serializable
import java.math.BigDecimal
import java.util.*

@Serializable
data class ServiceCartResponse(
    @Serializable(with = UUIDSerializer::class)
    val customerId: UUID,
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

