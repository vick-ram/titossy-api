package com.example.plugins.auth

import com.example.auth.Config.ISSUER
import com.example.auth.Config.REALM
import com.example.auth.Config.SECRET
import com.example.auth.makeJWTVerifier
import com.example.commands.queries.customer.getCustomerByEmail
import com.example.commands.queries.customer.getCustomerByStatus
import com.example.commands.queries.employee.getEmployeeByEmail
import com.example.commands.queries.supplier.getSupplierByEmail
import com.example.commands.queries.supplier.getSupplierByStatus
import com.example.exceptions.AccountPendingException
import com.example.exceptions.UnAuthorizedAccessException
import com.example.exceptions.UserDoesNotExist
import com.example.models.util.ApprovalStatus
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*

fun Application.configureAuthentication() {
    install(Authentication) {
        jwt("auth-jwt") {
            verifier(makeJWTVerifier(ISSUER, SECRET))
            realm = REALM

            validate { credential ->
                val email = credential.payload.getClaim("email").asString()
                val role = credential.payload.getClaim("role").asString()

                val customer = email?.let { getCustomerByEmail(it) }
                val supplier = email?.let { getSupplierByEmail(it) }
                val employee = email?.let { getEmployeeByEmail(it) }

                when {
                    customer != null -> {
                        val customerStatus = getCustomerByStatus(email)?.status
                        if (customerStatus == ApprovalStatus.APPROVED) {
                            JWTPrincipal(credential.payload)
                        } else {
                            throw AccountPendingException("Your account is pending approval")
                        }
                    }

                    supplier != null -> {
                        val supplierStatus = getSupplierByStatus(email)?.status
                        if (supplierStatus == ApprovalStatus.APPROVED) {
                            JWTPrincipal(credential.payload)
                        } else {
                            throw AccountPendingException("Your account is pending approval")
                        }
                    }

                    employee != null && role != null -> {
                        JWTPrincipal(credential.payload)
                    }

                    else -> {
                        throw UserDoesNotExist("User does not exist")
                    }
                }
            }

            challenge { _, _ ->
                throw UnAuthorizedAccessException("You need to sign in to access this resource")
            }
        }
    }
}