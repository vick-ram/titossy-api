package com.example.commons

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.withRole(vararg role: String, build: Route.() -> Unit) {
    val route = createChild(object : RouteSelector() {
        override fun evaluate(context: RoutingResolveContext, segmentIndex: Int): RouteSelectorEvaluation {
            return RouteSelectorEvaluation.Transparent
        }
    })
    route.install(RoleAuthorizationPlugin) {
        roles(setOf(role))
    }
    route.build()
}

class RoleBasedConfiguration {
    val requiredRoles = mutableSetOf<String>()
    fun roles(roles: Set<Array<out String>>) {
        roles.forEach { requiredRoles.addAll(it) }
    }
}

val RoleAuthorizationPlugin = createRouteScopedPlugin(
    "RoleAuthorizationPlugin",
    ::RoleBasedConfiguration
) {
    on(AuthenticationChecked) { call ->
        val principal = call.principal<JWTPrincipal>() ?: return@on
        val role = principal.payload.getClaim("role").asString() ?: ""

        if (pluginConfig.requiredRoles.isNotEmpty() && !pluginConfig.requiredRoles.contains(role)
        ) {
            call.respond(
                ApiResponse.error(
                    HttpStatusCode.Unauthorized,
                    "You do not have necessary permissions to access this resource. Please check your role and try again."

                )
            )
        }
    }
}