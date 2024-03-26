package com.example.commands.queries.feedback


import com.example.db.booking.Booking
import com.example.db.customer.Customer
import com.example.db.feedback.Feedback
import com.example.db.util.DatabaseUtil.dbQuery
import com.example.models.feedback.FeedbackRequest
import com.example.models.feedback.FeedbackResponse
import org.jetbrains.exposed.dao.with
import java.time.ZoneId
import java.util.*

private fun Feedback.toFeedbackResponse() = FeedbackResponse(
    feedbackId = this.id.value,
    customerId = this.customerId.id.value,
    bookingId = this.bookingId.id.value,
    feedback = this.feedback,
    rating = this.rating,
    date = Date.from(this.createdAt.atZone(ZoneId.systemDefault()).toInstant())
)

suspend fun giveFeedback(feedbackRequest: FeedbackRequest): FeedbackResponse = dbQuery {
    return@dbQuery try {
        Feedback.new {
            this.customerId = Customer[feedbackRequest.customerId]
            this.bookingId = Booking[feedbackRequest.bookingId]
            this.feedback = feedbackRequest.feedback
            this.rating = feedbackRequest.rating
        }.toFeedbackResponse()
    } catch (e: Exception) {
        throw e
    }
}

suspend fun getFeedbackById(feedbackId: Int): FeedbackResponse? = dbQuery {
    return@dbQuery try {
        Feedback.findById(feedbackId)?.toFeedbackResponse()
    } catch (e: Exception) {
        throw e
    }
}

suspend fun getFeedbackByBookingId(bookingId: Int): List<FeedbackResponse> = dbQuery {
    return@dbQuery try {
        Feedback.all().with(Feedback::bookingId).filter { it.bookingId.id.value == bookingId }
            .map { it.toFeedbackResponse() }
    } catch (e: Exception) {
        throw e
    }
}

suspend fun getFeedbackByCustomerId(customerId: Int): List<FeedbackResponse> = dbQuery {
    return@dbQuery try {
        Feedback.all().with(Feedback::customerId).filter { it.customerId.id.value == customerId }
            .map { it.toFeedbackResponse() }
    } catch (e: Exception) {
        throw e
    }
}

suspend fun deleteFeedback(id: Int): Boolean = dbQuery {
    return@dbQuery try {
        Feedback.findById(id)?.delete()
        true
    }catch (e: Exception){
        throw e
    }
}