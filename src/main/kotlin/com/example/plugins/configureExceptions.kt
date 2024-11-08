package com.example.plugins

import com.example.commons.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import kotlinx.serialization.SerializationException
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.valiktor.ConstraintViolationException

fun Application.configureException() {
    install(StatusPages) {

        exception<Throwable> { call, cause ->
            when (cause) {
                is ConstraintViolationException -> {
                    val errors = cause.constraintViolations.mapToMessages()
                        .groupBy({ it.first }, { it.second })
                        .mapValues { (_, value) -> value }
                    call.respond(
                        ApiResponse.error(
                            HttpStatusCode.BadRequest,
                            errors
                        )
                    )
                }

                is SerializationException -> {
                    call.respond(
                        ApiResponse.error(
                            HttpStatusCode.BadRequest,
                            cause.message
                        )
                    )
                }

                is AlreadyExistsException -> {
                    call.respond(
                        ApiResponse.error(
                            HttpStatusCode.Conflict,
                            cause.message
                        )
                    )
                }

                is ExposedSQLException -> {
                    call.respond(
                        ApiResponse.error(
                            HttpStatusCode.InternalServerError,
                            cause.message
                        )
                    )
                }

                is NoRecordFoundException -> {
                    call.respond(
                        ApiResponse.error(
                            HttpStatusCode.NotFound,
                            cause.message
                        )
                    )
                }

                is UnAuthorizedAccessException -> {
                    call.respond(
                        ApiResponse.error(
                            HttpStatusCode.Unauthorized,
                            cause.message ?: "Unauthorized access"
                        )
                    )
                }

                else -> {
                    call.respond(
                        ApiResponse.error(
                            HttpStatusCode.InternalServerError,
                            cause.message
                        )
                    )
                }
            }
        }

        status(HttpStatusCode.NotFound) { call, status ->
            call.respond(
                ApiResponse.error(
                    status,
                    "${status.value}: Page Not Found"
                )
            )
        }
        status(HttpStatusCode.BadRequest) { call, status ->
            call.respond(
                ApiResponse.error(
                    status,
                    "${status.value}: Couldn't process data, not well formatted"
                )
            )
        }
        status(HttpStatusCode.UnsupportedMediaType) { call, status ->
            call.respond(
                ApiResponse.error(
                    status,
                    status.description
                )
            )
        }
    }
}