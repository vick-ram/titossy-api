package com.example.routing

import com.example.auth.TokenBlackList
import com.example.commands.repo.customer.getCustomerByUsername
import com.example.commands.repo.customer.insertCustomer
import com.example.commands.repo.customer.signInCustomer
import com.example.commands.repo.customer.signOutCustomer
import com.example.models.Customer
import com.example.models.CustomerSignInData
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.authentication() {
    route("/customer/signUp") {
        post {
            val customer = call.receive(Customer::class)
            try {
                insertCustomer(customer)
                call.respond(HttpStatusCode.Created, "Customer created successfully")
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, "${e.message}")
            }
        }
    }

    route("/customer/signIn") {
        post {
            val customer = call.receive<CustomerSignInData>()
            try {
                val tokens = signInCustomer(customer.username, customer.password)
                tokens?.let {
                    call.respond(HttpStatusCode.OK, it)
                } ?: call.respond(HttpStatusCode.Unauthorized, "Invalid username or password")
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, "${e.message}")
            }
        }
    }


    route("/customer/signOut") {
        post {
            val token = call.request.headers["Authorization"].toString()
            try {
                signOutCustomer(token)
                call.respond(HttpStatusCode.OK, "Customer signed out successfully")
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, "${e.message}")
            }
        }
    }

    route("/customer/{username}") {
        authenticate("auth-jwt") {
            get {
                if (TokenBlackList.isTokenBlacklisted(call.request.headers["Authorization"].toString())) {
                    call.respond(HttpStatusCode.Unauthorized, "You need to login to access this resource")
                    return@get
                } else {
                    val username = call.parameters["username"]
                    val customer = username?.let { it1 -> getCustomerByUsername(it1) }
                    customer?.let {
                        call.respond(HttpStatusCode.OK, it)
                    } ?: call.respond(HttpStatusCode.NotFound, "Customer not found")
                }
            }
        }
    }
}