package com.example.commands.queries.customer

import com.example.auth.JwtPayload
import com.example.auth.TokenBlackList
import com.example.auth.generateTokens
import com.example.commands.customMatch
import com.example.db.customer.Customer
import com.example.db.customer.CustomerTable
import com.example.db.util.DatabaseUtil.dbQuery
import com.example.exceptions.*
import com.example.models.customer.CustomerRequest
import com.example.models.customer.CustomerResponse
import com.example.models.customer.CustomerSignInData
import com.example.models.customer.StatusUpdate
import com.example.models.util.ApprovalStatus
import com.example.models.util.comparePassword
import com.example.models.util.hashedPassword
import io.ktor.server.plugins.*
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.jetbrains.exposed.sql.selectAll
import java.time.LocalDateTime
import java.util.*

suspend fun createCustomer(
    customerRequest: CustomerRequest,
    secret: String
): CustomerResponse = dbQuery {
    val existingCustomer = CustomerTable
        .selectAll().where { CustomerTable.email eq customerRequest.email }.count() > 0
    if (existingCustomer) {
        throw AlreadyExistsException("Customer already exists")
    }
    return@dbQuery Customer.new {
        this.username = customerRequest.username
        this.fullName = "${customerRequest.firstName} ${customerRequest.lastName}"
        this.phone = customerRequest.phone
        this.email = customerRequest.email
        this.password = hashedPassword(customerRequest.password, secret)
        this.status = ApprovalStatus.PENDING
        this.createdAt = LocalDateTime.now()
        this.updatedAt = LocalDateTime.now()
        this.tsv = "to_tsvector('english', '${this.username} ${this.fullName} ${this.email}')"
    }.toCustomerResponse()
}

suspend fun signInCustomer(
    customerSignInData: CustomerSignInData,
    secret: String,
    issuer: String,
    audience: String
): String = dbQuery {
    return@dbQuery try {

        val customer = when {
            customerSignInData.email != null -> Customer.find { CustomerTable.email eq customerSignInData.email }
            customerSignInData.username != null -> Customer.find { CustomerTable.username eq customerSignInData.username }
            else -> null
        }?.singleOrNull()

        if (customer != null && !comparePassword(customerSignInData.password, customer.password, secret)) {
            throw InvalidCredentials("Wrong password provided for ${customer.email}")
        }

        customer?.let { cust ->
            generateTokens(
                JwtPayload(
                    sub = cust.id.toString(),
                    email = cust.email,
                    username = cust.username,
                    exp = Date(System.currentTimeMillis() + 31_536_000_000),
                    iss = issuer,
                    secret = secret,
                    audience = audience
                )
            )
        } ?: throw NotFoundException("User with ${customerSignInData.email ?: customerSignInData.username} not found")
    } catch (e: Exception) {
        when (e) {
            is NotFoundException -> throw e
            is InvalidCredentials -> throw e
            else -> throw UnexpectedError("An unexpected error occurred")
        }
    }
}

suspend fun signOutCustomer(token: String) = dbQuery {
    try {
        TokenBlackList.blacklistToken(token)
    } catch (e: Exception) {
        when (e) {
            is InvalidToken -> throw e
            is TokenAlreadyBlacklisted -> throw e
            else -> throw UnexpectedError("An unexpected error occurred during sign out")
        }
    }
}

suspend fun updateCustomerStatus(id: UUID, statusUpdate: StatusUpdate): CustomerResponse = dbQuery {
    return@dbQuery try {
        Customer.findByIdAndUpdate(id) {
            it.status = statusUpdate.status
            it.updatedAt = LocalDateTime.now()
        }?.toCustomerResponse()
            ?: throw NotFoundException("Could not find customer with $id id")
    } catch (e: Exception) {
        throw FailedToCreate("Failed to update customer status to ${statusUpdate.status}")
    }
}

suspend fun updateCustomer(id: UUID, customerUpdateRequest: CustomerRequest, secret: String): CustomerResponse =
    dbQuery {
        return@dbQuery try {
            Customer.findByIdAndUpdate(id) { customerUpdate ->
                customerUpdate.username = customerUpdateRequest.username
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

suspend fun deleteCustomer(id: UUID): Boolean = dbQuery {
    return@dbQuery try {
        Customer.findById(id)
            ?.delete()
            ?: throw NotFoundException("No customer with $id")
        true
    } catch (e: Exception) {
        when (e) {
            is ExposedSQLException -> throw e
            is DeleteException -> throw DeleteException("Could not delete the customer, failed")
            else -> throw e
        }
    }
}


suspend fun filteredCustomers(filter: (Customer) -> Boolean): List<CustomerResponse>? = dbQuery {
    return@dbQuery try {
        Customer.all()
            .limit(50)
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
            .selectAll().where { CustomerTable.tsv.customMatch(query) }
            .map { Customer.wrapRow(it).toCustomerResponse() }
            .toList()
    } catch (e: Exception) {
        println("Error while searching for customers: ${e.message}")
        null
    }
}


