package com.example.models.booking

import kotlinx.serialization.Serializable

@Serializable
data class UpdateBookingStatus(
    val bookingId: Int,
    val status: BookingStatus
)
