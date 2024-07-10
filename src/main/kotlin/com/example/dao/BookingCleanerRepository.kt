package com.example.dao

import com.example.db.booking.BookingAssignment
import com.example.db.booking.BookingCleanerResponse
import com.example.models.booking.BookingAssign
import java.util.UUID

interface BookingCleanerRepository {
    suspend fun assignBookingToCleaner(
        supervisorId: UUID,
        bookingAssign: BookingAssign
    ): Boolean

    suspend fun getFilteredAssignBookings(
        filter: (BookingAssignment) -> Boolean
    ): List<BookingCleanerResponse>

    suspend fun updateBookingCleaner(bookingId: String, cleanerId: UUID): Boolean
    suspend fun deleteBookingCleaner(bookingId: String): Boolean
}