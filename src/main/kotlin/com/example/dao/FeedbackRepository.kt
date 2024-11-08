package com.example.dao

import com.example.db.Feedback
import com.example.models.FeedbackRequest
import com.example.models.FeedbackResponse

interface FeedbackRepository {
    suspend fun createFeedback(
        customerId: String,
        feedbackRequest: FeedbackRequest
    ): FeedbackResponse
    suspend fun getFilteredFeedback(
        filter: (Feedback) -> Boolean
    ): List<FeedbackResponse>
    suspend fun updateFeedback(
        id: String,
        customerId: String,
        feedbackRequest: FeedbackRequest
    ): Boolean
    suspend fun deleteFeedback(id: String): Boolean
}