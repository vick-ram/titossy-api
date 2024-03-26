package com.example.models.feedback

import com.example.models.util.DateSerializer
import kotlinx.serialization.Serializable
import java.util.Date

@Serializable
data class FeedbackResponse(
    val feedbackId: Int,
    val customerId: Int,
    val bookingId: Int,
    val feedback: String,
    val rating: Double,
    @Serializable(with = DateSerializer::class)
    val date: Date
)
