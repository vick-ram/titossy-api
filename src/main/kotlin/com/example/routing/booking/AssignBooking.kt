package com.example.routing.booking

import com.example.commands.queries.booking.BookingCleanerRepositoryImpl
import com.example.dao.BookingCleanerRepository
import com.example.exceptions.ApiResponse
import com.example.models.booking.BookingAssign
import com.example.models.employee.Roles
import com.example.routing.withRole
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.*

fun Route.assignBookingRoutes() {
    val bookingDao: BookingCleanerRepository = BookingCleanerRepositoryImpl()

    route("/api/booking") {

        authenticate("auth-jwt") {
            withRole(Roles.SUPERVISOR.name) {
                post("/assign") {
                    try {
                        val principal = call.principal<JWTPrincipal>()
                        val supervisorId = principal?.subject.let { UUID.fromString(it) }
                        val bookingAssign = call.receive<BookingAssign>()
                        bookingDao.assignBookingToCleaner(
                            supervisorId,
                            bookingAssign
                        )
                        call.respond(
                            ApiResponse.success(
                                HttpStatusCode.Created,
                                null,
                                "Booking assigned to cleaner"
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
            }
        }

        get("/assign/{bookingId}") {
            try {
                val bookingId = call.parameters["bookingId"] ?: return@get
                val cleaners = bookingDao.getFilteredAssignBookings {
                    it.booking.bookingId.value == bookingId
                }
                call.respond(
                    ApiResponse.success(
                        HttpStatusCode.OK,
                        cleaners,
                        null
                    )
                )
            } catch (e: Exception) {
                e.printStackTrace()
                call.respond(
                    ApiResponse.error(
                        HttpStatusCode.BadRequest,
                        e.message ?: "An error occurred",
                    )
                )
            }
        }

        authenticate("auth-jwt") {
            get("/assign/cleaner") {
                val principal = call.principal<JWTPrincipal>()
                val cleanerId = principal?.subject ?: return@get
                try {
                    call.respond(
                        ApiResponse.success(
                            HttpStatusCode.OK,
                            bookingDao.getFilteredAssignBookings {
                                it.cleaner.id.value == UUID.fromString(cleanerId)
                            },
                            null
                        )
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                    call.respond(
                        ApiResponse.error(
                            HttpStatusCode.InternalServerError,
                            e.message ?: "An error occurred"

                        )
                    )
                }
            }
        }

        get("/assign") {
            try {
                call.respond(
                    ApiResponse.success(
                        HttpStatusCode.OK,
                        bookingDao.getFilteredAssignBookings { true },
                        null
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

                    else -> call.respond(
                        ApiResponse.error(
                            HttpStatusCode.InternalServerError,
                            e.message ?: "An error occurred"
                        )
                    )
                }
            }
        }

        put("/assign/{bookingId}") {
            try {
                val bookingId = call.parameters["bookingId"] ?: return@put
                val bookingAssign = call.receive<BookingAssign>()
                bookingDao.updateBookingCleaner(
                    bookingId,
                    bookingAssign.cleanerId
                )
                call.respond(
                    ApiResponse.success(
                        HttpStatusCode.Accepted,
                        null,
                        "Booking updated"
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

        delete("/assign/{bookingId}") {
            try {
                val bookingId = call.parameters["bookingId"] ?: return@delete
                bookingDao.deleteBookingCleaner(bookingId)
                call.respond(
                    ApiResponse.success(
                        HttpStatusCode.NoContent,
                        null,
                        "Booking assignment deleted"
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
    }
}