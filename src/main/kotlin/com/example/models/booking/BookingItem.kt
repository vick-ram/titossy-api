package com.example.models.booking

import com.example.models.util.BigDecimalSerializer
import com.example.models.util.LocalDateTimeSerializer
import com.example.models.util.UUIDSerializer
import kotlinx.serialization.Serializable
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

@Serializable
data class BookingServiceAddOnResponse(
    val service: String?,
    val serviceAddOn: String?,
    val quantity: Int,
    @Serializable(with = BigDecimalSerializer::class)
    val subtotal: BigDecimal,
)

