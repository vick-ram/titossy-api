package com.example.routing.feedback

import com.example.commands.queries.feedback.FeedbackRepositoryImpl
import com.example.dao.FeedbackRepository
import com.example.exceptions.ApiResponse
import com.example.exceptions.FailedToCreate
import com.example.models.feedback.FeedbackRequest
import com.example.routing.util.Feedback
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.plugins.*
import io.ktor.server.resources.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.*

fun Route.feedbackRoutes() {
    val feedbackRepo: FeedbackRepository = FeedbackRepositoryImpl()
    authenticate("auth-jwt") {
        post<Feedback, FeedbackRequest> { _, feedbackReq ->
            try {
                val principal = call.principal<JWTPrincipal>()
                val customerId = principal?.subject ?: return@post
                val feedback = feedbackReq.validate()
                call.respond(
                    ApiResponse.success(
                        HttpStatusCode.Created,
                        feedbackRepo.createFeedback(
                            customerId = UUID.fromString(customerId),
                            feedbackRequest = feedback
                        ),
                        "Feedback posted"
                    )
                )
            } catch (e: Exception) {
                when (e) {
                    is FailedToCreate -> call.respond(
                        ApiResponse.error(
                            HttpStatusCode.Conflict,
                            e.message
                        )
                    )
                }
            }
        }
    }

    get<Feedback> { query ->
        try {
            when {
                query.customerId != null -> call.respond(
                    ApiResponse.success(
                        HttpStatusCode.OK,
                        feedbackRepo.getFilteredFeedback { it.customerId?.id?.value == query.customerId },
                        null
                    )
                )

                query.message != null -> call.respond(
                    ApiResponse.success(
                        HttpStatusCode.OK,
                        feedbackRepo.getFilteredFeedback { it.feedback == query.message },
                        null
                    )
                )

                query.date != null -> call.respond(
                    ApiResponse.success(
                        HttpStatusCode.OK,
                        feedbackRepo.getFilteredFeedback { it.createdAt == query.date },
                        null
                    )
                )

                else -> call.respond(
                    ApiResponse.success(
                        HttpStatusCode.OK,
                        feedbackRepo.getFilteredFeedback { true },
                        null
                    )
                )
            }
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

    get<Feedback.Id> { param ->
        try {
            call.respond(
                ApiResponse.success(
                    HttpStatusCode.OK,
                    feedbackRepo.getFilteredFeedback { it.id.value == param.id }.firstOrNull(),
                    "Retrieved successfully"
                )
            )
        } catch (e: Exception) {
            when (e) {
                is NotFoundException -> call.respond(
                    ApiResponse.error(
                        HttpStatusCode.NotFound,
                        e.message
                    )
                )

                is IllegalArgumentException -> call.respond(
                    ApiResponse.error(
                        HttpStatusCode.Conflict,
                        e.message
                    )
                )
            }
        }
    }

    delete<Feedback.Id> { param ->
        try {
            feedbackRepo.deleteFeedback(param.id)
            call.respond(
                ApiResponse.success(
                    HttpStatusCode.NoContent,
                    null,
                    "Deleted successfully"
                )
            )
        } catch (e: Exception) {
            when (e) {
                is NotFoundException -> call.respond(
                    ApiResponse.error(
                        HttpStatusCode.NotFound,
                        e.message
                    )
                )

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