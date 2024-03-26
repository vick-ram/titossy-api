package com.example.routing.customer

import com.example.commands.queries.customer.*
import com.example.exceptions.AccountPendingException
import com.example.models.customer.CustomerUpdate
import com.example.models.customer.StatusUpdate
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.customerRoutes() {
    route("/customer") {
        get {
            val customers = getAllCustomers()
            try {
                customers.let {
                    call.respond(HttpStatusCode.OK, it)
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, "${e.message}")
            }
        }
        get("/{id}") {
            val id = call.parameters["id"]?.toInt() ?: 0
            try {
                val customer = getCustomerById(id)
                customer?.let {
                    call.respond(HttpStatusCode.OK, it)
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, "${e.message}")
            }
        }
        /*get("/username/{username}") {
            val username = call.parameters["username"] ?: ""
            try {
                val customer = getCustomerByUsername(username)
                customer?.let {
                    call.respond(HttpStatusCode.OK, it)
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, "${e.message}")
            }
        }*/

        put("{id}") {
            val id = call.parameters["id"]?.toInt() ?: 0
            val customer = call.receive<CustomerUpdate>()
            try {
                updateCustomer(id, customer)
                call.respond(HttpStatusCode.Created, "Customer updated successfully")
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, "${e.message}")
            }

        }
        patch("/{id}") {
            val id = call.parameters["id"]?.toInt() ?: 0
            val customerStatus = call.receive<StatusUpdate>()
            try {
                updateCustomerStatus(id, customerStatus.status)
                call.respond(HttpStatusCode.Created, "Customer status updated successfully")
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, "${e.message}")
            }
        }

        delete("/{id}") {
            val id = call.parameters["id"]?.toInt() ?: 0
            try {
                val deletedCustomer = deleteCustomer(id)
                deletedCustomer.let {
                    call.respond(HttpStatusCode.OK, "Customer deleted successfully")
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.Conflict, "${e.message}")
            }
        }

        authenticate("auth-jwt") {
            get("/authenticated/{username}") {
                val username = call.parameters["username"] ?: ""
                try {
                    val customer = getCustomerByUsername(username)
                    customer?.let {
                        call.respond(HttpStatusCode.OK, it)
                    }
                } catch (e: AccountPendingException) {
                    call.respond(HttpStatusCode.BadRequest, "${e.message}")
                }
            }
        }
    }
}