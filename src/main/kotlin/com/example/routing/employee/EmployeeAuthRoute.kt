package com.example.routing.employee

import com.example.commands.queries.employee.createEmployee
import com.example.commands.queries.employee.signInEmployee
import com.example.commands.queries.employee.signOutEmployee
import com.example.models.employee.EmployeeCredentials
import com.example.models.employee.EmployeeRequest
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.employeeAuthRoute() {
    route("/employee") {
        post("/signUp") {
            val employee = call.receive<EmployeeRequest>()
            try {
                createEmployee(employee)
                call.respond(HttpStatusCode.Created, "Employee created successfully")

            } catch (e: Exception) {
                call.respond("${e.message}")
            }
        }
        post("/signIn") {
            val employee = call.receive<EmployeeCredentials>()
            try {
                val token = signInEmployee(employee.email, employee.password)
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
                signOutEmployee(token)
                call.respondRedirect("/employee/signIn")
            } catch (e: Exception) {
                call.respond("${e.message}")
            }
        }
    }
}