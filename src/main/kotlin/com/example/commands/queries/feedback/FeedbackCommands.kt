package com.example.commands.queries.feedback


import com.example.dao.FeedbackRepository
import com.example.db.booking.Booking
import com.example.db.customer.Customer
import com.example.db.feedback.Feedback
import com.example.db.feedback.FeedbackTable
import com.example.db.util.DatabaseUtil.dbQuery
import com.example.models.feedback.FeedbackRequest
import com.example.models.feedback.FeedbackResponse
import org.jetbrains.exposed.sql.selectAll
import java.util.*

class FeedbackRepositoryImpl : FeedbackRepository {
    override suspend fun createFeedback(customerId: UUID, feedbackRequest: FeedbackRequest): FeedbackResponse =
        dbQuery {
            val newFeedback = Feedback.new {
                this.customerId = Customer[customerId]
            this.bookingId = Booking[feedbackRequest.bookingId]
            this.feedback = feedbackRequest.feedback
            this.rating = feedbackRequest.rating
            }
            return@dbQuery newFeedback.toFeedbackResponse()
        }

    override suspend fun getFilteredFeedback(filter: (Feedback) -> Boolean): List<FeedbackResponse> = dbQuery {
        return@dbQuery Feedback.all()
            .limit(20, 4)
            .filter(filter)
            .sortedByDescending { it.createdAt.coerceAtLeast(it.updatedAt) }
            .map { it.toFeedbackResponse() }
    }

    override suspend fun updateFeedback(id: UUID, customerId: UUID, feedbackRequest: FeedbackRequest): Boolean =
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

    override suspend fun deleteFeedback(id: UUID): Boolean = dbQuery {
        val feedbackExists = FeedbackTable.selectAll().where { FeedbackTable.id eq id }.count() > 0
        if (!feedbackExists) {
            throw IllegalArgumentException("Feedback with id $id could not be found")
        }
        Feedback.findById(id)?.delete()
        return@dbQuery true
    }
}