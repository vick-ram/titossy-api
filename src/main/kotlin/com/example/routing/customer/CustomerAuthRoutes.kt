package com.example.routing.customer

import com.example.commands.queries.customer.*
import com.example.exceptions.AccountPendingException
import com.example.models.customer.CustomerRequest
import com.example.models.customer.CustomerSignInData
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.customerAuthRoutes() {
    route("/customer") {
        post("/signUp") {
            val customer = call.receive(CustomerRequest::class)
            try {
                insertCustomer(customer)
                call.respond(HttpStatusCode.Created, "Customer created successfully")
            } catch (e: AccountPendingException) {
                call.respond(e.message.toString())
            }
        }
        post("/signIn") {
            val customer = call.receive<CustomerSignInData>()
            try {
                val token = signInCustomer(customer.email, customer.password)
                token?.let {
                    call.respond(HttpStatusCode.OK, it)
                }
            } catch (e: Exception) {
                call.respond("${e.message}")
            }
        }
        post("/signOut") {
            val token = call.request.headers["Authorization"].toString()
            try {
                signOutCustomer(token)
                call.respondRedirect("/customer/signIn")
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, "${e.message}")
            }
        }
    }
}