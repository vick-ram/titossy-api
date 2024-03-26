package com.example.models.booking

import kotlinx.serialization.Serializable


@Serializable
data class BookingItemRequest(
    val serviceId: Int,
    val bookingId: Int,
    val quantity: Int
)
@Serializable
data class BookingItemResponse(
    val id: Int,
    val serviceId: Int,
    val bookingId: Int,
    val quantity: Int,
    val price: Double,
    val duration: Int
)
