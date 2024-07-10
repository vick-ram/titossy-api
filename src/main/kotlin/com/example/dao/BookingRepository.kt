package com.example.dao

import com.example.db.booking.Booking
import com.example.models.booking.BookingRequest
import com.example.models.booking.BookingResponse
import com.example.models.booking.UpdateBookingStatus
import java.util.UUID

interface BookingRepository {
    suspend fun createNewBooking(customerId: UUID, bookingRequest: BookingRequest): BookingResponse
    suspend fun getFilteredBookings(filter: (Booking) -> Boolean): List<BookingResponse>
    suspend fun updateBookingStatus(id: String, bookingStatusUpdate: UpdateBookingStatus): Boolean
    suspend fun updateBooking(customerId: UUID, id: String, bookingUpdateRequest: BookingRequest): Boolean
    suspend fun deleteBooking(id: String): Boolean
    suspend fun clearBookings(): Boolean
}