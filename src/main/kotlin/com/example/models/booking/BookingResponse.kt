package com.example.models.booking

import com.example.models.util.*
import kotlinx.serialization.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.*

enum class Frequency {
    ONE_TIME,
    WEEKLY,
    BIWEEKLY,
    MONTHLY
}

@Serializable
data class BookingResponse(
    val bookingId: String,
    val customer: String,
    @Serializable(with = LocalDateTimeSerializer::class)
    val bookingDateTime: LocalDateTime,
    val frequency: Frequency,
    val additionalInstructions: String? = null,
    val address: String?,
    val bookingServiceAddOns: List<BookingServiceAddOnResponse>,
    @Serializable(with = BigDecimalSerializer::class)
    val totalAmount: BigDecimal,
    val paid: Boolean,
    val bookingStatus: BookingStatus,
    @Serializable(with = LocalDateTimeSerializer::class)
    val createdAt: LocalDateTime,
    @Serializable(with = LocalDateTimeSerializer::class)
    val updatedAt: LocalDateTime
) : java.io.Serializable

