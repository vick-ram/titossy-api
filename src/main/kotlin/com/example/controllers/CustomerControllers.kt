package com.example.controllers

import com.example.commons.*
import com.example.commons.DatabaseUtil.dbQuery
import com.example.db.ActivityLogEntity
import com.example.db.Booking
import com.example.db.Bookings
import com.example.db.Customer
import com.example.db.CustomerTable
import com.example.db.NotificationEntity
import com.example.models.CustomerRequest
import com.example.models.CustomerResponse
import com.example.models.CustomerSignInData
import com.example.models.StatusUpdate
import io.ktor.server.plugins.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.jetbrains.exposed.sql.selectAll
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

suspend fun createCustomer(
    customerRequest: CustomerRequest,
    secret: String,
    scope: CoroutineScope
): CustomerResponse = dbQuery {
    val existingCustomer = CustomerTable
        .selectAll()
        .where { CustomerTable.email eq customerRequest.email }
        .count() > 0
    if (existingCustomer) {
        throw AlreadyExistsException("Customer already exists")
    }
    val customer = Customer.new {
        this.fullName = "${customerRequest.firstName} ${customerRequest.lastName}"
        this.phone = customerRequest.phone
        this.email = customerRequest.email
        this.password = hashedPassword(customerRequest.password, secret)
        this.status = ApprovalStatus.PENDING
        this.createdAt = LocalDateTime.now()
        this.updatedAt = LocalDateTime.now()
        this.tsv = "to_tsvector('english', '${this.fullName} ${this.email}')"
    }
    val readableDate = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss").format(LocalDateTime.now())

    scope.launch {
        logUserEvent(
            userId = customer.id.value,
            event = EventType.REGISTRATION,
            eventData = "Customer ${customer.fullName} created at $readableDate"
        )
    }
    return@dbQuery customer.toCustomerResponse()
}

suspend fun signInCustomer(
    customerSignInData: CustomerSignInData,
    secret: String,
    issuer: String,
    audience: String,
    scope: CoroutineScope
): String = dbQuery {
    return@dbQuery try {
        val customer = Customer.find { CustomerTable.email.eq(customerSignInData.email) }.singleOrNull()
        if (customer != null && !comparePassword(customerSignInData.password, customer.password, secret)) {
            throw InvalidCredentials("Wrong password provided for ${customer.email}")
        }

        val readableDate = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss").format(LocalDateTime.now())

        scope.launch {
            logUserEvent(
                userId = customer?.id?.value ?: "",
                event = EventType.LOGIN,
                eventData = "Customer ${customer?.fullName} logged in at $readableDate"
            )
        }

        customer?.let { cust ->
            generateToken(
                JwtPayload(
                    sub = cust.id.toString(),
                    email = cust.email,
                    exp = Date(System.currentTimeMillis() + 31_536_000_000),
                    iss = issuer,
                    secret = secret,
                    audience = audience
                )
            )
        } ?: throw NotFoundException("User with ${customerSignInData.email} not found")
    } catch (e: Exception) {
        when (e) {
            is NotFoundException -> throw e
            is InvalidCredentials -> throw e
            else -> throw UnexpectedError("An unexpected error occurred")
        }
    }
}

suspend fun signOutCustomer(token: String, scope: CoroutineScope) = dbQuery {
    try {
        TokenBlackList.blacklistToken(token)
        scope.launch {
            logUserEvent(
                userId = "",
                event = EventType.LOGOUT,
                eventData = "Customer logged out at ${LocalDateTime.now()}"
            )
        }
    } catch (e: Exception) {
        when (e) {
            is InvalidToken -> throw e
            is TokenAlreadyBlacklisted -> throw e
            else -> throw UnexpectedError("An unexpected error occurred during sign out")
        }
    }
}

suspend fun updateCustomerStatus(
    id: String,
    statusUpdate: StatusUpdate,
    scope: CoroutineScope
): CustomerResponse = dbQuery {
    return@dbQuery try {
        val readableDate = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss").format(LocalDateTime.now())
        Customer.findByIdAndUpdate(id) { update ->
            update.status = statusUpdate.status
            update.updatedAt = LocalDateTime.now()
            scope.launch {
                logUserEvent(
                    userId = id,
                    event = EventType.UPDATE,
                    eventData = "Customer status updated to ${statusUpdate.status} at $readableDate"
                )
            }
        }?.toCustomerResponse()
            ?: throw NotFoundException("Could not find customer with $id id")
    } catch (e: Exception) {
        throw FailedToCreate("Failed to update customer status to ${statusUpdate.status}")
    }
}

suspend fun updateCustomer(
    id: String,
    customerUpdateRequest: CustomerRequest,
    secret: String
): CustomerResponse =
    dbQuery {
        return@dbQuery try {
            Customer.findByIdAndUpdate(id) { customerUpdate ->
                customerUpdate.fullName = "${customerUpdateRequest.firstName} ${customerUpdateRequest.lastName}"
                customerUpdate.phone = customerUpdateRequest.phone
                customerUpdate.email = customerUpdateRequest.email
                customerUpdate.password = hashedPassword(customerUpdateRequest.password, secret)
                customerUpdate.updatedAt = LocalDateTime.now()
            }?.toCustomerResponse()
                ?: throw NotFoundException("No customer with $id id")
        } catch (e: Exception) {
            when (e) {
                is ExposedSQLException -> throw e
                is FailedToCreate -> throw FailedToCreate("Failed to update customer with $id id details")
                else -> throw e
            }
        }

    }

suspend fun deleteCustomer(id: String, scope: CoroutineScope): Boolean = dbQuery {
    return@dbQuery try {
        Customer.findById(id)
            ?.delete()
            ?: throw NotFoundException("No customer with $id")
        val customer = Customer.find { CustomerTable.id.eq(id) }.singleOrNull()
        val readableDate = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss").format(LocalDateTime.now())
        scope.launch {
            logUserEvent(
                userId = id,
                event = EventType.DELETE,
                eventData = "Customer ${customer?.fullName} deleted at $readableDate"
            )
        }
        true
    } catch (e: Exception) {
        when (e) {
            is ExposedSQLException -> throw e
            is DeleteException -> throw DeleteException("Could not delete the customer, failed")
            else -> throw e
        }
    }
}


suspend fun filteredCustomers(
    filter: (Customer) -> Boolean
): List<CustomerResponse>? = dbQuery {
    return@dbQuery try {
        Customer.all()
            .filter(filter)
            .sortedByDescending { it.createdAt.coerceAtLeast(it.updatedAt) }
            .map { it.toCustomerResponse() }
    } catch (e: Exception) {
        null
    }
}

suspend fun searchCustomer(query: String): List<CustomerResponse>? = dbQuery {
    return@dbQuery try {
        CustomerTable
            .selectAll()
            .where { CustomerTable.tsv.customMatch(query) }
            .map { Customer.wrapRow(it).toCustomerResponse() }
            .toList()
    } catch (e: Exception) {
        null
    }
}

suspend fun forgotPassword(
    changePasswordRequest: ChangePasswordRequest,
    secret: String,
    scope: CoroutineScope
): Boolean = dbQuery {
    return@dbQuery try {
        val customer = Customer.find { CustomerTable.email.eq(changePasswordRequest.email) }.singleOrNull()
            ?: throw NotFoundException("Customer with email ${changePasswordRequest.email} not found")

        customer.password = hashedPassword(changePasswordRequest.newPassword, secret)
        customer.updatedAt = LocalDateTime.now()

        val readableDate = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss").format(LocalDateTime.now())
        scope.launch {
            logUserEvent(
                userId = customer.id.value,
                event = EventType.PASSWORD_RESET,
                eventData = "Customer ${customer.fullName} password reset at $readableDate"
            )
        }
        true
    } catch (e: Exception) {
        when (e) {
            is NotFoundException -> throw e
            else -> throw UnexpectedError("An unexpected error occurred")
        }
    }
}

suspend fun getCustomerDetails(id: String) = dbQuery {
    /*This function gets all details including the joins*/
    val customer = Customer.findById(id)

    customer?.apply {
        // Bookings (one-to-many)
        val bookings = Booking.find { Bookings.customerId.eq(id) }
    }
}

