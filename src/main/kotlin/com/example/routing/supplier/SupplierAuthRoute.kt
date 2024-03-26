package com.example.routing.supplier

import com.example.commands.queries.supplier.createSupplier
import com.example.commands.queries.supplier.signInSupplier
import com.example.commands.queries.supplier.signOutSupplier
import com.example.exceptions.AccountPendingException
import com.example.models.supplier.SupplierRequest
import com.example.models.supplier.SupplierSignInData
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.supplierAuthRoutes() {

    route("/supplier") {
        post("/signUp") {
            val supplier = call.receive<SupplierRequest>()
            try {
                val supplierResponse = createSupplier(supplier)
                supplierResponse.let {
                    call.respond(HttpStatusCode.Created)
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, "${e.message}")
            }
        }

        post("/signIn") {
            val supplier = call.receive<SupplierSignInData>()
            try {
                val tokens = signInSupplier(supplier.email, supplier.password)
                tokens?.let {
                    call.respond(HttpStatusCode.OK, it)
                } ?: call.respond(HttpStatusCode.Unauthorized, "Invalid username or password")
            } catch (e: AccountPendingException) {
                call.respond(HttpStatusCode.Unauthorized, "${e.message}")
            }
        }

        post("/signOut") {
            val token = call.request.headers["Authorization"].toString()
            try {
                signOutSupplier(token)
                call.respondRedirect("/supplier/signIn")
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, "${e.message}")
            }
        }

    }
}