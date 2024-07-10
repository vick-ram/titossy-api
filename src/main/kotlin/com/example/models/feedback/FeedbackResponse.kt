package com.example.models.feedback

import com.example.models.util.LocalDateTimeSerializer
import com.example.models.util.UUIDSerializer
import kotlinx.serialization.Serializable
import java.time.LocalDateTime
import java.util.*

@Serializable
data class FeedbackResponse(
    @Serializable(with = UUIDSerializer::class)
    val feedbackId: UUID,
    @Serializable(with = UUIDSerializer::class)
    val customerId: UUID?,
    val bookingId: String,
    val feedback: String,
    val rating: Double,
    @Serializable(with = LocalDateTimeSerializer::class)
    val createdAt: LocalDateTime,
    @Serializable(with = LocalDateTimeSerializer::class)
    val updatedAt: LocalDateTime
)
