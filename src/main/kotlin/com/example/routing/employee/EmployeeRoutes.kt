package com.example.routing.employee

import com.example.commands.queries.employee.deleteEmployee
import com.example.commands.queries.employee.getAllEmployees
import com.example.commands.queries.employee.getEmployeeById
import com.example.commands.queries.employee.updateEmployee
import com.example.models.employee.EmployeeUpdate
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.employeeRoutes() {
    route("/employee") {
        get {
            val employees = getAllEmployees()
            try {
                employees.let {
                    call.respond(HttpStatusCode.OK, it)
                }
            } catch (e: Exception) {
                call.respond("${e.message}")

            }
        }
        get("/{id}") {
            val id = call.parameters["id"]?.toInt() ?: 0
            val employee = getEmployeeById(id)
            try {
                employee?.let {
                    call.respond(HttpStatusCode.OK, it)
                }
            } catch (e: Exception) {
                call.respond("${e.message}")
            }
        }
        put("/{id}") {
            val id = call.parameters["id"]?.toInt() ?: 0
            val employee = call.receive<EmployeeUpdate>()

            try {
                updateEmployee(id, employee)
                call.respond(HttpStatusCode.OK, "Employee updated successfully")
            } catch (e: Exception) {
                call.respond("${e.message}")
            }
        }
        authenticate("auth-jwt") {
            delete("/{id}") {
                val id = call.parameters["id"]?.toInt() ?: 0
                try {
                    deleteEmployee(id)
                    call.respond(HttpStatusCode.Conflict, "Employee deleted successfully")
                } catch (e: Exception) {
                    call.respond("${e.message}")
                }
            }
        }

        /*withRole("MANAGER") {
            webSocket("/chat") {
                val role = call.principal<JWTPrincipal>("role")?.toString()
                incoming.consumeEach {
                    if (it is Frame.Text) {
                        outgoing.send(Frame.Text("You said: ${it.readText()}"))
                    }
                }
                incoming.receiveAsFlow().map {
                    if (it is Frame.Text) {
                        outgoing.send(Frame.Text("You said: ${it.readText()}"))
                    } else {
                        "Error: Frame is not text"
                    }
                }
                for (frame in incoming) {
                    if (frame is Frame.Text) {
                        val text = frame.readText()
                        outgoing.send(Frame.Text("You said: $text"))
                    }
                }
                try {
                    outgoing.send(Frame.Text("You are connected as a $role"))
                } catch (e: Exception) {
                    outgoing.send(Frame.Text("Error: ${e.message}"))
                }
            }
        }*/
    }
}