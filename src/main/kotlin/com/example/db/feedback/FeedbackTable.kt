package com.example.db.feedback

import com.example.db.booking.Booking
import com.example.db.booking.BookingTable
import com.example.db.customer.Customer
import com.example.db.customer.CustomerTable
import com.example.db.service.Service
import com.example.db.service.ServiceTable
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.javatime.datetime
import java.time.LocalDateTime

object FeedbackTable: IntIdTable() {
    val customerId = reference("customer_id", CustomerTable, onDelete = ReferenceOption.CASCADE)
    val bookingId = reference("service_id", BookingTable, onDelete = ReferenceOption.CASCADE)
    val feedback = varchar("feedback", 255)
    val rating = double("rating")
    val createdAt = datetime("created_at").clientDefault { LocalDateTime.now() }
}

class Feedback(id: EntityID<Int>): IntEntity(id){
    companion object: IntEntityClass<Feedback>(FeedbackTable)

    var customerId by Customer referencedOn FeedbackTable.customerId
    var bookingId by Booking referencedOn FeedbackTable.bookingId
    var feedback by FeedbackTable.feedback
    var rating by FeedbackTable.rating
    var createdAt by FeedbackTable.createdAt
}