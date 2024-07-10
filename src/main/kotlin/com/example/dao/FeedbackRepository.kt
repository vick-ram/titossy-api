package com.example.dao

import com.example.db.feedback.Feedback
import com.example.models.feedback.FeedbackRequest
import com.example.models.feedback.FeedbackResponse
import java.util.UUID

interface FeedbackRepository {
    suspend fun createFeedback(customerId: UUID, feedbackRequest: FeedbackRequest): FeedbackResponse
    suspend fun getFilteredFeedback(filter: (Feedback) -> Boolean): List<FeedbackResponse>
    suspend fun updateFeedback(id: UUID, customerId: UUID, feedbackRequest: FeedbackRequest): Boolean
    suspend fun deleteFeedback(id: UUID): Boolean
}