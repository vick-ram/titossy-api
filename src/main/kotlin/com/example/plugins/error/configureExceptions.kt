package com.example.plugins.error

import com.example.exceptions.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import org.valiktor.ConstraintViolationException

fun Application.configureException() {
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            call.respondText(text = "500: $cause", status = HttpStatusCode.InternalServerError)
        }
        status(HttpStatusCode.NotFound) { call, status ->
            call.respondText("404: page not found", status = status)
        }
        //Valiktor validation
        exception<ConstraintViolationException> { call, cause ->
            call.respond(HttpStatusCode.BadRequest, cause.constraintViolations.joinToString())
        }
        exception<AccountPendingException> { call, cause ->
            call.respond(HttpStatusCode.Forbidden, cause)
        }

        exception<UnAuthorizedAccessException> { call, cause ->
            call.respond(HttpStatusCode.Unauthorized, cause)
        }

        exception<UserDoesNotExist> { call, cause ->
            call.respond(HttpStatusCode.Conflict, cause)
        }
        exception<AlreadyExistsException> { call, cause ->
            call.respond(HttpStatusCode.Conflict, cause)
        }
        exception<FailedToValidate> { call, cause ->
            call.respond(HttpStatusCode.ExpectationFailed, cause)
        }
    }
}