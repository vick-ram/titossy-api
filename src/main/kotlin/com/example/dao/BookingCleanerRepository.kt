package com.example.dao

import com.example.db.BookingAssignment
import com.example.db.BookingCleanerResponse
import com.example.models.BookingAssign

interface BookingCleanerRepository {
    suspend fun assignBookingToCleaner(
        supervisorId: String,
        bookingAssign: BookingAssign
    ): Boolean

    suspend fun getFilteredAssignBookings(
        filter: (BookingAssignment) -> Boolean
    ): List<BookingCleanerResponse>

    suspend fun updateBookingCleaner(bookingId: String, cleanerId: String): Boolean
    suspend fun deleteBookingCleaner(bookingId: String): Boolean
}