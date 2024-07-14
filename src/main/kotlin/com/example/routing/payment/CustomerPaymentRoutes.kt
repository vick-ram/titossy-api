package com.example.routing.payment

import com.example.commands.queries.payment.CustomerPaymentRepositoryImpl
import com.example.dao.CustomerPaymentRepository
import com.example.exceptions.ApiResponse
import com.example.models.payment.CustomerPaymentRequest
import com.example.models.payment.PaymentStatus
import com.example.models.payment.PaymentUpdateStatus
import com.example.routing.util.Payment
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.resources.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.customerPaymentRoutes() {
    val customerPaymentRepo: CustomerPaymentRepository = CustomerPaymentRepositoryImpl()

    post<Payment.Customer, CustomerPaymentRequest> { _, paymentRequest ->
        try {
            val p = paymentRequest.validate()
            call.respond(
                ApiResponse.success(
                    HttpStatusCode.Created,
                    customerPaymentRepo.createPayment(p),
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

                is NotFoundException -> call.respond(
                    ApiResponse.error(
                        HttpStatusCode.NotFound,
                        e.message
                    )
                )
            }
        }
    }

    get<Payment.Customer> { query ->
        try {
            when {
                query.paymentStatus != null -> call.respond(
                    ApiResponse.success(
                        HttpStatusCode.OK,
                        customerPaymentRepo.filteredCustomerPayment {
                            it.paymentStatus == PaymentStatus.valueOf(query.paymentStatus)
                        },
                        null
                    )
                )

                query.phone != null -> call.respond(
                    ApiResponse.success(
                        HttpStatusCode.OK,
                        customerPaymentRepo.filteredCustomerPayment {
                            it.phone == query.phone
                        },
                        null
                    )
                )

                query.refNumber != null -> call.respond(
                    ApiResponse.success(
                        HttpStatusCode.OK,
                        customerPaymentRepo.filteredCustomerPayment {
                            it.refNumber == query.refNumber
                        }.firstOrNull(),
                        null
                    )
                )

                query.date != null -> call.respond(
                    ApiResponse.success(
                        HttpStatusCode.OK,
                        customerPaymentRepo.filteredCustomerPayment {
                            it.createdAt == query.date
                        },
                        null
                    )
                )

                else -> call.respond(
                    ApiResponse.success(
                        HttpStatusCode.OK,
                        customerPaymentRepo.filteredCustomerPayment { true },
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

    get<Payment.Customer.Id> { param ->
        try {
            call.respond(
                ApiResponse.success(
                    HttpStatusCode.OK,
                    customerPaymentRepo.filteredCustomerPayment {
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

    put<Payment.Customer.Id, CustomerPaymentRequest> { param, paymentUpdateRequest ->
        try {
            customerPaymentRepo.updateCustomerPayment(
                param.id,
                paymentUpdateRequest.validate()
            )
            call.respond(
                ApiResponse.success(
                    HttpStatusCode.OK,
                    null,
                    "Customer payment successfully updated"
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

    patch<Payment.Customer.Id, PaymentUpdateStatus> { param, statusUpdate ->
        try {
            customerPaymentRepo.updatePaymentStatus(
                param.id,
                statusUpdate.validate()
            )
            call.respond(
                ApiResponse.success(
                    HttpStatusCode.Accepted,
                    null,
                    "Customer payment status successfully updated"
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

    delete<Payment.Customer.Id> { param ->
        try {
            customerPaymentRepo.deleteCustomerPayment(param.id)
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