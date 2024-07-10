package com.example.models.booking

import com.example.models.util.UUIDSerializer
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class BookingAssign(
    val bookingId: String,
    @Serializable(with = UUIDSerializer::class)
    val cleanerId: UUID
)
