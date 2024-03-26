package com.example.routing.feedback

import com.example.commands.queries.feedback.*
import com.example.models.feedback.FeedbackRequest
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.feedbackRoutes(){
    route("/feedback"){
        post {
            val feedback = call.receive<FeedbackRequest>()
            try {
                val postFeedback = giveFeedback(feedback)
                call.respond(postFeedback)
            }catch (e: Exception) {
                call.respondText("Error: ${e.message}")
            }
        }

        get("/{feedbackId}"){
            val feedbackId = call.parameters["feedbackId"]?.toInt() ?: return@get call.respondText("Invalid feedback id")
            try {
                val feedback = getFeedbackById(feedbackId)
                feedback?.let { call.respond(it) } ?: run { call.respondText("Feedback not found") }
            }catch (e: Exception) {
                call.respondText("Error: ${e.message}")
            }
        }

        get("/booking/{bookingId}"){
            val bookingId = call.parameters["bookingId"]?.toInt() ?: return@get call.respondText("Invalid booking id")
            try {
                val feedback = getFeedbackByBookingId(bookingId)
                call.respond(feedback)
            }catch (e: Exception) {
                call.respondText("Error: ${e.message}")
            }
        }

        get("/customer/{customerId}"){
            val customerId = call.parameters["customerId"]?.toInt() ?: return@get call.respondText("Invalid customer id")
            try {
                val feedback = getFeedbackByCustomerId(customerId)
                call.respond(feedback)
            }catch (e: Exception) {
                call.respondText("Error: ${e.message}")
            }
        }

        delete("/{feedbackId}"){
            val id = call.parameters["id"]?.toInt() ?: return@delete call.respondText("Invalid feedback id")
            try {
                val feedback = deleteFeedback(id)
                call.respond(feedback)
            }catch (e: Exception) {
                call.respondText("Error: ${e.message}")
            }
        }
    }
}