package com.example.dao

import com.example.db.Booking
import com.example.models.BookingRequest
import com.example.models.BookingResponse
import com.example.models.UpdateBookingStatus
import kotlinx.coroutines.CoroutineScope

interface BookingRepository {
    suspend fun createNewBooking(
        customerId: String,
        bookingRequest: BookingRequest,
        scope: CoroutineScope
    ): BookingResponse

    suspend fun getFilteredBookings(
        filter: (Booking) -> Boolean
    ): List<BookingResponse>

    suspend fun updateBookingStatus(
        id: String,
        bookingStatusUpdate: UpdateBookingStatus,
        scope: CoroutineScope
    ): Boolean

    suspend fun updateBooking(
        customerId: String,
        id: String,
        bookingUpdateRequest: BookingRequest
    ): Boolean
    suspend fun deleteBooking(id: String): Boolean
    suspend fun clearBookings(): Boolean
}