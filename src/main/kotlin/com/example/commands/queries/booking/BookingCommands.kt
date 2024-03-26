package com.example.commands.queries.booking

import com.example.db.booking.Booking
import com.example.db.booking.BookingItem
import com.example.db.booking.BookingItemsTable
import com.example.db.booking.BookingTable
import com.example.db.customer.Customer
import com.example.db.employee.Employee
import com.example.db.service.Service
import com.example.db.util.DatabaseUtil.dbQuery
import com.example.models.booking.BookingItemResponse
import com.example.models.booking.BookingRequest
import com.example.models.booking.BookingResponse
import com.example.models.booking.BookingStatus
import org.jetbrains.exposed.sql.and
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

private fun Booking.toBookingResponse() = BookingResponse(
    bookingId = this.id.value,
    customerId = this.customer.id.value,
    employeeId = this.employee.id.value,
    bookingDate = Date.from(this.bookingDate.atZone(ZoneId.systemDefault()).toInstant()),
    scheduledDate = Date.from(this.scheduledDate.atZone(ZoneId.systemDefault()).toInstant()),
    bookingItems = this.bookingItems.map { it.toBookingItemResponse() },
    totalAmount = this.totalAmount.toDouble(),
    bookingStatus = this.bookingStatus
)

private fun BookingItem.toBookingItemResponse() = BookingItemResponse(
    id = this.id.value,
    serviceId = this.service.id.value,
    bookingId = this.booking.id.value,
    quantity = this.quantity,
    price = this.price.toDouble(),
    duration = this.duration
)

suspend fun createBooking(bookingRequest: BookingRequest): BookingResponse = dbQuery {
    return@dbQuery try {
        val newBooking = Booking.new {
            this.customer = Customer[bookingRequest.customerId]
            this.employee = Employee[bookingRequest.employeeId]
            this.bookingDate = LocalDateTime.now()
            this.scheduledDate =
                LocalDateTime.ofInstant(bookingRequest.scheduleDate.toInstant(), ZoneId.systemDefault())
            this.totalAmount = bookingItems.sumOf { it.price * it.quantity.toBigDecimal() }
            this.bookingStatus = BookingStatus.PENDING
        }
        bookingRequest.bookingItems.forEach { item ->
            BookingItem.new {
                this.service = Service[item.serviceId]
                this.booking = newBooking
                this.quantity = item.quantity
                this.price = Service[item.serviceId].price
                this.duration = Service[item.serviceId].duration
            }
        }
        newBooking.toBookingResponse()
    } catch (e: Exception) {
        throw  e
    }
}

suspend fun getBooking(bookingId: Int): BookingResponse? = dbQuery {
    return@dbQuery try {
        val booking = Booking.findById(bookingId) ?: return@dbQuery null
        booking.toBookingResponse()
    } catch (e: Exception) {
        null
    }
}

suspend fun getBookings(): List<BookingResponse> = dbQuery {
    return@dbQuery try {
        Booking.all().map { it.toBookingResponse() }
    } catch (e: Exception) {
        emptyList()
    }
}

suspend fun getBookingsByCustomerId(customerId: Int): List<BookingResponse> = dbQuery {
    return@dbQuery try {
        Booking.find { BookingTable.customer eq customerId }.map { it.toBookingResponse() }
    } catch (e: Exception) {
        emptyList()
    }
}

suspend fun getBookingsByEmployeeId(employeeId: Int): List<BookingResponse> = dbQuery {
    return@dbQuery try {
        Booking.find { BookingTable.employee eq employeeId }.map { it.toBookingResponse() }
    } catch (e: Exception) {
        emptyList()
    }
}

suspend fun getBookingsByStatus(status: BookingStatus): List<BookingResponse> = dbQuery {
    return@dbQuery try {
        Booking.find { BookingTable.bookingStatus eq status }.map { it.toBookingResponse() }
    } catch (e: Exception) {
        emptyList()
    }
}

suspend fun getBookingsByDate(date: Date): List<BookingResponse> = dbQuery {
    return@dbQuery try {
        val localDateTime = LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault())
        val startOfDay = localDateTime.withHour(0).withMinute(0).withSecond(0).withNano(0)
        Booking.find {
            BookingTable.scheduledDate greaterEq startOfDay and
                    (BookingTable.scheduledDate less localDateTime.plusDays(1).withHour(0).withMinute(0).withSecond(0)
                        .withNano(0))
        }.map { it.toBookingResponse() }
    } catch (e: Exception) {
        emptyList()
    }
}


suspend fun updateBookingStatus(bookingId: Int, status: BookingStatus): Boolean = dbQuery {
    return@dbQuery try {
        val booking = Booking.findById(bookingId) ?: return@dbQuery false
        booking.bookingStatus = status
        true
    } catch (e: Exception) {
        false
    }
}

suspend fun deleteBooking(bookingId: Int): Boolean = dbQuery {
    return@dbQuery try {
        val booking = Booking.findById(bookingId) ?: return@dbQuery false
        BookingItem.find { BookingItemsTable.booking eq bookingId }.forEach { it.delete() }
        booking.delete()
        true
    } catch (e: Exception) {
        false
    }
}

/*suspend fun getBookingItems(bookingId: Int): List<BookingItemResponse> = dbQuery {
    return@dbQuery try {
        BookingItem.find { BookingItemsTable.booking eq bookingId }.map { it.toBookingItemResponse() }
    } catch (e: Exception) {
        emptyList()
    }
}*/


