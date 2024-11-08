package com.example.routing

import com.example.commons.ApiResponse
import com.example.commons.FailedToCreate
import com.example.commons.Feedback
import com.example.controllers.FeedbackRepositoryImpl
import com.example.controllers.getActivityLogs
import com.example.controllers.getUnreadNotifications
import com.example.controllers.markNotificationAsRead
import com.example.dao.FeedbackRepository
import com.example.models.FeedbackRequest
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.plugins.*
import io.ktor.server.resources.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

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
                            customerId = customerId,
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

fun Route.activitiesRoutes() {
    route("/api/activities") {
        get("/logs") {
            try {
                call.respond(
                    ApiResponse.success(
                        HttpStatusCode.OK,
                        getActivityLogs(),
                        null
                    )
                )
            } catch (e: Exception) {
                e.printStackTrace()
                call.respond(
                    ApiResponse.error(
                        HttpStatusCode.BadRequest,
                        e.message
                    )
                )
            }
        }
        route("/notifications") {
            get {
                try {
                    call.respond(
                        ApiResponse.success(
                            HttpStatusCode.OK,
                            getUnreadNotifications(),
                            null
                        )
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                    call.respond(
                        ApiResponse.error(
                            HttpStatusCode.BadRequest,
                            e.message
                        )
                    )
                }
            }

            patch("/{id}") {
                try {
                    val notificationId = call.parameters["id"] ?: return@patch
                    markNotificationAsRead(notificationId)
                    call.respond(
                        ApiResponse.success(
                            HttpStatusCode.OK,
                            null,
                            "Notification marked as read"
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
                        else -> call.respond(
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
}