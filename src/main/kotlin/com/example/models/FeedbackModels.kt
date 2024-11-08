package com.example.models

import com.example.commons.LocalDateTimeSerializer
import kotlinx.serialization.Serializable
import org.valiktor.functions.hasSize
import org.valiktor.functions.isBetween
import java.time.LocalDateTime

@Serializable
data class FeedbackRequest(
    val bookingId: String,
    val feedback: String,
    val rating: Double
){
    fun validate(): FeedbackRequest {
        org.valiktor.validate(this) {
            validate(FeedbackRequest::feedback).hasSize(min = 5, max = 500)
            validate(FeedbackRequest::rating).isBetween(0.0, 5.0)
        }
        return this
    }
}

@Serializable
data class FeedbackResponse(
    val feedbackId: String,
    val customerId: String?,
    val bookingId: String,
    val feedback: String,
    val rating: Double,
    @Serializable(with = LocalDateTimeSerializer::class)
    val createdAt: LocalDateTime,
    @Serializable(with = LocalDateTimeSerializer::class)
    val updatedAt: LocalDateTime
)
