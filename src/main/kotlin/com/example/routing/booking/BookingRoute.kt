package com.example.routing.booking

import com.example.commands.queries.booking.BookingCache
import com.example.commands.queries.booking.BookingRepositoryImpl
import com.example.dao.BookingRepository
import com.example.exceptions.ApiResponse
import com.example.exceptions.UnexpectedError
import com.example.models.booking.BookingRequest
import com.example.models.booking.BookingStatus
import com.example.models.booking.UpdateBookingStatus
import com.example.routing.util.Booking
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.resources.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.io.File
import java.util.*

fun Route.bookingRoute() {
    val dao: BookingRepository = BookingCache(
        BookingRepositoryImpl(),
        environment?.config?.property("storage.ehcacheFilePath")?.getString()?.let { File(it) }
    )
    authenticate("auth-jwt") {
        post<Booking, BookingRequest> { _, bookingReq ->
            try {

                val principal = call.principal<JWTPrincipal>()
                val customerId = principal?.subject
                call.respond(
                    ApiResponse.success(
                        HttpStatusCode.Created,
                        customerId?.let { dao.createNewBooking(UUID.fromString(it), bookingReq) },
                        "Successfully created booking"
                    )
                )
            } catch (e: Exception) {
                when (e) {
                    is IllegalArgumentException -> call.respond(
                        ApiResponse.error(
                            HttpStatusCode.BadRequest,
                            e.message
                        )
                    )

                    is UnexpectedError -> call.respond(
                        ApiResponse.error(
                            HttpStatusCode.InternalServerError,
                            e.message
                        )
                    )

                    else -> call.respond(
                        ApiResponse.error(
                            HttpStatusCode.InternalServerError,
                            e.message
                        )
                    )
                }
            }
        }
    }

    authenticate("auth-jwt") {
        put<Booking.Id, BookingRequest> { param, bookingUpdate ->
            val principal = call.principal<JWTPrincipal>()
            val customerId = principal?.subject
            try {
                customerId?.let {
                    dao.updateBooking(
                        UUID.fromString(it),
                        param.id, bookingUpdate
                    )
                }
                call.respond(
                    ApiResponse.success(
                        HttpStatusCode.Accepted,
                        null,
                        "Booking successfully updated"
                    )
                )
            } catch (e: Exception) {
                call.respond(
                    ApiResponse.error(
                        HttpStatusCode.Conflict,
                        e.message
                    )
                )
            }
        }
    }

    patch<Booking.Id, UpdateBookingStatus> { param, bookingUpdate ->
        try {
            val updatedBooking = dao.updateBookingStatus(param.id, bookingUpdate)
            if (updatedBooking)
                dao.updateBookingStatus(param.id, bookingUpdate)
            call.respond(
                ApiResponse.success(
                    HttpStatusCode.OK,
                    null,
                    "Successfully updated booking status"
                )
            )
        } catch (e: Exception) {
            call.respond(
                ApiResponse.error(
                    HttpStatusCode.InternalServerError,
                    e.message
                )
            )
        }
    }

    get<Booking> { param ->
        when {
            param.status != null -> call.respond(
                ApiResponse.success(
                    HttpStatusCode.OK,
                    dao.getFilteredBookings {
                        it.bookingStatus == BookingStatus.valueOf(param.status)
                    },
                    null
                )
            )

            param.date != null -> call.respond(
                ApiResponse.success(
                    HttpStatusCode.OK,
                    dao.getFilteredBookings { it.bookingDateTime == param.date },
                    null
                )
            )

            else -> call.respond(
                ApiResponse.success(
                    HttpStatusCode.OK,
                    dao.getFilteredBookings { true },
                    null
                )
            )
        }
    }

    authenticate("auth-jwt") {
        get("/api/booking/customer") {
            try {
                val principal = call.principal<JWTPrincipal>()
                val customerId = principal?.subject?.let { UUID.fromString(it) } ?: return@get
                call.respond(
                    ApiResponse.success(
                        HttpStatusCode.OK,
                        dao.getFilteredBookings { it.customer.id.value == customerId },
                        null
                    )
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    get<Booking.Id> { param ->
        call.respond(
            ApiResponse.success(
                HttpStatusCode.OK,
                dao.getFilteredBookings { it.id.value == param.id }.firstOrNull(),
                null
            )
        )
    }


    delete<Booking.Id> { param ->
        try {
            dao.deleteBooking(param.id)
            call.respond(
                ApiResponse.success(
                    HttpStatusCode.NoContent,
                    null,
                    "Successfully deleted booking"
                )
            )
        } catch (e: Exception) {
            when (e) {
                is IllegalArgumentException -> call.respond(
                    ApiResponse.error(
                        HttpStatusCode.BadRequest,
                        e.message
                    )
                )
            }
        }
    }
    delete("/api/booking/clear") {
        try {
            dao.clearBookings()
            call.respond(
                ApiResponse.success(
                    HttpStatusCode.NoContent,
                    null,
                    "Successfully cleared bookings"
                )
            )
        } catch (e: Exception) {
            call.respond(
                ApiResponse.error(
                    HttpStatusCode.InternalServerError,
                    e.message
                )
            )
        }
    }
}