package com.example.routing

import com.example.commons.*
import com.example.controllers.BookingCache
import com.example.controllers.BookingCleanerRepositoryImpl
import com.example.controllers.BookingRepositoryImpl
import com.example.dao.BookingCleanerRepository
import com.example.dao.BookingRepository
import com.example.models.BookingAssign
import com.example.models.BookingRequest
import com.example.models.UpdateBookingStatus
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.resources.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.io.File

fun Route.assignBookingRoutes() {
    val bookingDao: BookingCleanerRepository = BookingCleanerRepositoryImpl()

    route("/api/booking") {

        authenticate("auth-jwt") {
            withRole(Roles.SUPERVISOR.name) {
                post("/assign") {
                    try {
                        val principal = call.principal<JWTPrincipal>()
                        val supervisorId = principal?.subject
                        val bookingAssign = call.receive<BookingAssign>()
                        supervisorId?.let { bookingDao.assignBookingToCleaner(it, bookingAssign) }
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
                                it.cleaner.id.value == cleanerId
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
                        customerId?.let { dao.createNewBooking(it, bookingReq, this) },
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
                        it,
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
            val updatedBooking = dao.updateBookingStatus(param.id, bookingUpdate, this)
            if (updatedBooking)
                dao.updateBookingStatus(param.id, bookingUpdate, this)
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
                val customerId = principal?.subject ?: return@get
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