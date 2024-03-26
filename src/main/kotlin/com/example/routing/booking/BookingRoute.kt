package com.example.routing.booking

import com.example.commands.queries.booking.*
import com.example.models.booking.BookingRequest
import com.example.models.booking.BookingStatus
import com.example.models.booking.UpdateBookingStatus
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

fun Route.bookingRoute() {
    route("/booking") {
        post {
            val bookingRequest = call.receive<BookingRequest>()
            val booking = createBooking(bookingRequest)
            try {
                booking.let { call.respond(booking) }
            } catch (e: Exception) {
                call.respond("Booking failed: ${e.message}")
            }
        }

        patch("/{id}") {
            val update = call.receive<UpdateBookingStatus>()
            try {
                val bookingStatus = updateBookingStatus(update.bookingId, update.status)
                call.respond(HttpStatusCode.OK, bookingStatus)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, "Booking update failed: ${e.message}")
            }
        }

        get {
            val allBookings = getBookings()
            try {
                allBookings.forEach { call.respond(it) }
            } catch (e: Exception) {
                call.respond("Booking failed: ${e.message}")
            }
        }

        get("/date/{date}"){
            val dateString = call.parameters["date"]
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            val date = LocalDate.parse(dateString, formatter)
            val bookingResponse = getBookingsByDate(Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant()))
            if (bookingResponse.isNotEmpty()) {
                call.respond(HttpStatusCode.OK, bookingResponse)
            } else {
                call.respond(HttpStatusCode.NotFound, "No orders found for the given date")
            }
        }

        get("/{id}") {
            val id = call.parameters["id"]?.toIntOrNull() ?: return@get call.respond("Invalid id")
            try {
                val booking = getBooking(id)
                booking?.let { call.respond(it) }
            } catch (e: Exception) {
                call.respond("${e.message}")
            }
        }

        get("/status/{status}") {
            val status = call.parameters["status"]?.let { BookingStatus.valueOf(it) }
                ?: return@get call.respond("Invalid status")
            try {
                val booking = getBookingsByStatus(status)
                booking.forEach { call.respond(it) }
            } catch (e: Exception) {
                call.respond("${e.message}")
            }
        }

        get("/customer/{customerId}") {
            val customerId = call.parameters["customerId"]?.toIntOrNull() ?: return@get call.respond("Invalid id")
            try {
                val booking = getBookingsByCustomerId(customerId)
                booking.forEach { call.respond(it) }
            } catch (e: Exception) {
                call.respond("${e.message}")
            }
        }

        get("/employee/{employeeId}") {
            val employeeId = call.parameters["employeeId"]?.toIntOrNull() ?: return@get call.respond("Invalid id")
            try {
                val booking = getBookingsByEmployeeId(employeeId)
                booking.forEach { call.respond(it) }
            } catch (e: Exception) {
                call.respond("${e.message}")
            }
        }

        delete("/{id}") {
            val id = call.parameters["id"]?.toIntOrNull() ?: return@delete call.respond("Invalid id")
            try {
                val booking = deleteBooking(id)
                call.respond(booking)
            } catch (e: Exception) {
                call.respond("${e.message}")
            }
        }
    }
}