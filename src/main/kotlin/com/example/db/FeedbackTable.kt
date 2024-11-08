package com.example.db

import com.example.commons.StringUUIDEntity
import com.example.commons.StringUUIDEntityClass
import com.example.commons.StringUUIDTable
import com.example.models.FeedbackResponse
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.ReferenceOption

object FeedbackTable : StringUUIDTable("feedback") {
    val customerId = reference("customer_id", CustomerTable, onDelete = ReferenceOption.CASCADE).nullable()
    val bookingId = reference("feedback_id", Bookings, onDelete = ReferenceOption.CASCADE)
    val feedback = text("message", eagerLoading = true)
    val rating = double("rating")
}

class Feedback(id: EntityID<String>) : StringUUIDEntity(id, FeedbackTable) {
    companion object : StringUUIDEntityClass<Feedback>(FeedbackTable)

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