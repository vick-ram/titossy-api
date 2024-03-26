package com.example.models.booking

import com.example.models.util.DateSerializer
import kotlinx.serialization.Serializable
import java.util.Date

@Serializable
data class BookingResponse(
    val bookingId: Int,
    val customerId: Int,
    val employeeId: Int,
    @Serializable(with = DateSerializer::class)
    val bookingDate: Date,
    @Serializable(with = DateSerializer::class)
    val scheduledDate: Date,
    val bookingItems: List<BookingItemResponse>,
    val totalAmount: Double,
    val bookingStatus: BookingStatus
)
