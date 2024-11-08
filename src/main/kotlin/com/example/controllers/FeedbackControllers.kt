package com.example.controllers

import com.example.commons.DatabaseUtil.dbQuery
import com.example.commons.NotificationState
import com.example.dao.FeedbackRepository
import com.example.db.*
import com.example.models.FeedbackRequest
import com.example.models.FeedbackResponse
import com.example.models.Notification
import io.ktor.server.plugins.*
import org.jetbrains.exposed.sql.selectAll

class FeedbackRepositoryImpl : FeedbackRepository {
    override suspend fun createFeedback(
        customerId: String,
        feedbackRequest: FeedbackRequest
    ): FeedbackResponse =
        dbQuery {
            val newFeedback = Feedback.new {
                this.customerId = Customer[customerId]
                this.bookingId = Booking[feedbackRequest.bookingId]
                this.feedback = feedbackRequest.feedback
                this.rating = feedbackRequest.rating
            }
            return@dbQuery newFeedback.toFeedbackResponse()
        }

    override suspend fun getFilteredFeedback(
        filter: (Feedback) -> Boolean
    ): List<FeedbackResponse> = dbQuery {
        return@dbQuery Feedback.all()
            .filter(filter)
            .sortedByDescending { it.createdAt.coerceAtLeast(it.updatedAt) }
            .map { it.toFeedbackResponse() }
    }

    override suspend fun updateFeedback(id: String, customerId: String, feedbackRequest: FeedbackRequest): Boolean =
        dbQuery {
            val feedbackExists = FeedbackTable.selectAll().where { FeedbackTable.id eq id }.count() > 0
            if (!feedbackExists) {
                throw IllegalArgumentException("Feedback with id $id could not be found")
            }
            Feedback.findByIdAndUpdate(id) { update ->
                update.customerId = customerId.let { cust -> Customer[cust] }
                update.bookingId = Booking[feedbackRequest.bookingId]
                update.feedback = feedbackRequest.feedback
                update.rating = feedbackRequest.rating
            }
            return@dbQuery true
        }

    override suspend fun deleteFeedback(id: String): Boolean = dbQuery {
        val feedbackExists = FeedbackTable.selectAll().where { FeedbackTable.id eq id }.count() > 0
        if (!feedbackExists) {
            throw IllegalArgumentException("Feedback with id $id could not be found")
        }
        Feedback.findById(id)?.delete()
        return@dbQuery true
    }
}
