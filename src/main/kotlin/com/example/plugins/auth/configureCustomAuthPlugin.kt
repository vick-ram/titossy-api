package com.example.plugins.auth

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*

val authenticationPlugin = createRouteScopedPlugin(
    name = "authenticationPlugin",
    createConfiguration = ::PluginConfiguration,
    body = {
        pluginConfig.apply {
            on(AuthenticationChecked) { call ->
                val principal = call.principal<JWTPrincipal>()
                if (principal != null) {
                    val role = principal.payload.getClaim("role")?.asString() ?: ""
                    val roleFromDb = getRole.invoke(role)
                    if (roleFromDb == "Admin") {
                        call.respond(HttpStatusCode.OK, "Admin")
                    } else {
                        when (roleFromDb) {
                            "Manager" -> call.respond(HttpStatusCode.OK, "Manager")
                            "Finance" -> call.respond(HttpStatusCode.OK, "Finance")
                            "Inventory" -> call.respond(HttpStatusCode.OK, "Inventory")
                            "Supervisor" -> call.respond(HttpStatusCode.OK, "Supervisor")
                            "Cleaner" -> call.respond(HttpStatusCode.OK, "Cleaner")
                            else -> call.respond(HttpStatusCode.Unauthorized, "Invalid role")
                        }
                    }
                } else {
                    call.respond(HttpStatusCode.Unauthorized, "Invalid token")
                }
            }
        }
    }
)

class PluginConfiguration {
    lateinit var getRole: suspend (role: String?) -> String?
}