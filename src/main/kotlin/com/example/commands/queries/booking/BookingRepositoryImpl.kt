package com.example.commands.queries.booking

import com.example.dao.BookingRepository
import com.example.db.booking.Booking
import com.example.db.booking.BookingServiceAddOn
import com.example.db.booking.Bookings
import com.example.db.cart.ServiceCart
import com.example.db.cart.ServiceCartItem
import com.example.db.customer.Customer
import com.example.db.util.DatabaseUtil.dbQuery
import com.example.exceptions.UnexpectedError
import com.example.models.booking.*
import org.jetbrains.exposed.sql.selectAll
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

class BookingRepositoryImpl : BookingRepository {
    override suspend fun createNewBooking(customerId: UUID, bookingRequest: BookingRequest): BookingResponse = dbQuery {
        try {
            var totalAmount = BigDecimal.ZERO
            val bookingDateTime = LocalDateTime.of(bookingRequest.bookingDate, bookingRequest.bookingTime)
            val booking = Booking.new {
                this.customer = Customer[customerId]
                this.bookingDateTime = bookingDateTime
                this.frequency = bookingRequest.frequency
                this.additionalInstructions = bookingRequest.additionalInstructions
                this.address = bookingRequest.address
                this.totalAmount = totalAmount
                this.bookingStatus = BookingStatus.PENDING
            }
            val frequency = when (bookingRequest.frequency) {
                Frequency.ONE_TIME -> BigDecimal.ONE
                Frequency.WEEKLY -> BigDecimal("1.8")
                Frequency.BIWEEKLY -> BigDecimal("1.6")
                Frequency.MONTHLY -> BigDecimal("1.5")
            }
            val serviceCartItems = ServiceCartItem.find { ServiceCart.customerId eq customerId }.toList()
            if (serviceCartItems.isEmpty()) {
                throw IllegalArgumentException("Cannot create booking. The cart is empty")
            }

            serviceCartItems.forEach { cartItem ->
                val serviceTotalAmount = cartItem.toServiceCartResponse().total * frequency
                totalAmount += serviceTotalAmount

                cartItem.service?.let {
                    BookingServiceAddOn.new {
                        this.booking = booking
                        this.service = it
                        this.quantity = quantity
                        this.subtotal = it.price.times(cartItem.quantity.toBigDecimal())
                            .times(frequency)
                    }
                }
                cartItem.serviceAddon?.let {
                    BookingServiceAddOn.new {
                        this.booking = booking
                        this.serviceAddOn = it
                        this.quantity = cartItem.quantity
                        this.subtotal = it.price.times(cartItem.quantity.toBigDecimal())
                            .times(frequency)
                    }
                }
            }
            booking.totalAmount = totalAmount
            serviceCartItems.forEach { cartItem ->
                cartItem.delete()
            }
            return@dbQuery booking.toBookingResponse()
        } catch (e: Exception) {
            when (e) {
                is IllegalArgumentException -> throw e
                else -> {
                    println("UnExpected error: ${e.message}")
                    e.printStackTrace()
                    throw UnexpectedError("An unexpected error occurred")
                }
            }
        }
    }

    override suspend fun clearBookings(): Boolean = dbQuery {
        Booking.all()
            .forEach {
                it.delete()
            }
        return@dbQuery true
    }

    override suspend fun getFilteredBookings(
        filter: (Booking) -> Boolean
    ): List<BookingResponse> = dbQuery {
        return@dbQuery Booking.all()
            .filter(filter)
            .sortedByDescending { it.createdAt.coerceAtLeast(it.updatedAt) }
            .map { it.toBookingResponse() }
    }

    override suspend fun updateBookingStatus(
        id: String,
        bookingStatusUpdate: UpdateBookingStatus
    ): Boolean = dbQuery {
        val bookingExists = Bookings
            .selectAll()
            .where { Bookings.id eq id }
            .count() > 0
        if (!bookingExists) {
            throw IllegalArgumentException("Booking with id $id does not exist")
        }

        Booking.findByIdAndUpdate(id) { updateStatus ->
            updateStatus.bookingStatus = bookingStatusUpdate.status
        }
        return@dbQuery true
    }

    override suspend fun updateBooking(
        customerId: UUID,
        id: String,
        bookingUpdateRequest: BookingRequest
    ): Boolean = dbQuery {
        val bookingExists = Bookings.selectAll()
            .where { Bookings.id eq id }
            .count() > 0
        if (!bookingExists) {
            throw IllegalArgumentException("Booking with id $id does not exist")
        }
        val bookingDateTime = LocalDateTime.of(bookingUpdateRequest.bookingDate, bookingUpdateRequest.bookingTime)
        Booking.findByIdAndUpdate(id) { update ->
            update.customer = Customer[customerId]
            update.bookingDateTime = bookingDateTime
            update.frequency = bookingUpdateRequest.frequency
            update.additionalInstructions = bookingUpdateRequest.additionalInstructions
            update.address = bookingUpdateRequest.address
        }
        return@dbQuery true
    }

    override suspend fun deleteBooking(id: String): Boolean = dbQuery {
        val bookingExists = Bookings.selectAll()
            .where { Bookings.id eq id }
            .count() > 0
        if (!bookingExists) {
            throw IllegalArgumentException("Booking with id $id does not exist")
        }
        Booking.findById(id)?.delete()
        return@dbQuery true
    }
}
