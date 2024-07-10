package com.example.db.feedback

import com.example.db.booking.Booking
import com.example.db.booking.Bookings
import com.example.db.customer.Customer
import com.example.db.customer.CustomerTable
import com.example.db.util.CustomUUIDEntity
import com.example.db.util.CustomUUIDEntityClass
import com.example.db.util.CustomUUIDTable
import com.example.models.feedback.FeedbackResponse
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.ReferenceOption
import java.util.*

object FeedbackTable : CustomUUIDTable("feedback") {
    val customerId = reference("customer_id", CustomerTable, onDelete = ReferenceOption.CASCADE).nullable()
    val bookingId = reference("feedback_id", Bookings, onDelete = ReferenceOption.CASCADE)
    val feedback = text("message", eagerLoading = true)
    val rating = double("rating")
}

class Feedback(id: EntityID<UUID>) : CustomUUIDEntity(id, FeedbackTable) {
    companion object : CustomUUIDEntityClass<Feedback>(FeedbackTable)

    var customerId by Customer optionalReferencedOn FeedbackTable.customerId
    var bookingId by Booking referencedOn FeedbackTable.bookingId
    var feedback by FeedbackTable.feedback
    var rating by FeedbackTable.rating

    fun toFeedbackResponse() = FeedbackResponse(
        feedbackId = this.id.value,
        customerId = this.customerId?.id?.value,
        bookingId = bookingId.id.value,
        feedback = this.feedback,
        rating = this.rating,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}