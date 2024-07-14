package com.example.routing.payment

import com.example.commands.queries.payment.SupplierPaymentRepositoryImpl
import com.example.dao.SupplierPaymentRepository
import com.example.exceptions.ApiResponse
import com.example.models.payment.PaymentStatus
import com.example.models.payment.PaymentUpdateStatus
import com.example.models.payment.SupplierPaymentRequest
import com.example.routing.util.Payment
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.resources.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.*

fun Route.supplierPaymentRoutes() {

    val supplierPaymentRepo: SupplierPaymentRepository = SupplierPaymentRepositoryImpl()
    authenticate("auth-jwt") {
        post<Payment.Supplier, SupplierPaymentRequest> { _, paymentRequest ->
            try {
                val principal = call.principal<JWTPrincipal>()
                val employeeId = principal?.subject.let { UUID.fromString(it) }
                    ?: return@post
                call.respond(
                    ApiResponse.success(
                        HttpStatusCode.Created,
                        supplierPaymentRepo.createPayment(
                            employeeId = employeeId,
                            paymentRequest.validate()
                        ),
                        "Payment made successfully"
                    )
                )
            } catch (e: Exception) {
                when (e) {
                    is IllegalArgumentException -> call.respond(
                        ApiResponse.error(
                            HttpStatusCode.Conflict,
                            e.message
                        )
                    )
                }
            }
        }
    }

    get<Payment.Supplier> { query ->
        try {
            when {
                query.paymentStatus != null -> call.respond(
                    ApiResponse.success(
                        HttpStatusCode.OK,
                        supplierPaymentRepo.filteredSupplierPayment {
                            it.paymentStatus == PaymentStatus.valueOf(query.paymentStatus)
                        },
                        null
                    )
                )

                query.refNumber != null -> call.respond(
                    ApiResponse.success(
                        HttpStatusCode.OK,
                        supplierPaymentRepo.filteredSupplierPayment {
                            it.refNumber == query.refNumber
                        }.firstOrNull(),
                        null
                    )
                )

                query.date != null -> call.respond(
                    ApiResponse.success(
                        HttpStatusCode.OK,
                        supplierPaymentRepo.filteredSupplierPayment {
                            it.createdAt == query.date
                        },
                        null
                    )
                )

                else -> call.respond(
                    ApiResponse.success(
                        HttpStatusCode.OK,
                        supplierPaymentRepo.filteredSupplierPayment { true },
                        null
                    )
                )
            }
        } catch (e: Exception) {
            when (e) {
                is IllegalArgumentException -> call.respond(
                    ApiResponse.error(
                        HttpStatusCode.NotFound,
                        e.message
                    )
                )
            }
        }
    }

    get<Payment.Supplier.Id> { param ->
        try {
            call.respond(
                ApiResponse.success(
                    HttpStatusCode.OK,
                    supplierPaymentRepo.filteredSupplierPayment {
                        it.id.value == param.id
                    }.firstOrNull(),
                    null
                )
            )
        } catch (e: Exception) {
            when (e) {
                is IllegalArgumentException -> call.respond(
                    ApiResponse.error(
                        HttpStatusCode.BadRequest,
                        e.message
                    )
                )
            }
        }
    }

    authenticate("auth-jwt") {
    put<Payment.Supplier.Id, SupplierPaymentRequest> { param, paymentUpdateRequest ->
        try {
            val principal = call.principal<JWTPrincipal>()
            val employeeId = principal?.subject?.let { UUID.fromString(it) }
                ?: return@put
            supplierPaymentRepo.updateSupplierPayment(
                param.id,
                employeeId = employeeId,
                paymentUpdateRequest.validate()
            )
            call.respond(
                ApiResponse.success(
                    HttpStatusCode.OK,
                    null,
                    "payment successfully updated"
                )
            )
        } catch (e: Exception) {
            when (e) {
                is IllegalArgumentException -> call.respond(
                    ApiResponse.error(
                        HttpStatusCode.BadRequest,
                        e.message
                    )
                )
            }
        }
    }
    }

    patch<Payment.Supplier.Id, PaymentUpdateStatus> { param, statusUpdate ->
        try {
            supplierPaymentRepo.updatePaymentStatus(
                param.id,
                statusUpdate.validate()
            )
            call.respond(
                ApiResponse.success(
                    HttpStatusCode.Accepted,
                    null,
                    "payment status successfully updated"
                )
            )
        } catch (e: Exception) {
            when (e) {
                is IllegalArgumentException -> call.respond(
                    ApiResponse.error(
                        HttpStatusCode.BadRequest,
                        e.message
                    )
                )
            }
        }
    }

    delete<Payment.Supplier.Id> { param ->
        try {
            supplierPaymentRepo.deleteSupplierPayment(param.id)
            call.respond(
                ApiResponse.success(
                    HttpStatusCode.NoContent,
                    null,
                    "Successfully deleted payment"
                )
            )
        } catch (e: Exception) {
            when (e) {
                is IllegalArgumentException -> call.respond(
                    ApiResponse.error(
                        HttpStatusCode.BadRequest,
                        e.message
                    )
                )
            }
        }
    }
}