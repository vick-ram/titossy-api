package com.example.models.feedback

import kotlinx.serialization.Serializable
import org.valiktor.functions.hasSize
import org.valiktor.functions.isBetween
import org.valiktor.validate

@Serializable
data class FeedbackRequest(
    val bookingId: String,
    val feedback: String,
    val rating: Double
){
    fun validate(): FeedbackRequest {
        validate(this){
            validate(FeedbackRequest::feedback).hasSize(min = 5, max = 500)
            validate(FeedbackRequest::rating).isBetween(0.0, 5.0)
        }
        return this
    }
}
