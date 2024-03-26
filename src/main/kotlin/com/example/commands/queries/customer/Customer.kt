package com.example.commands.queries.customer

import com.example.auth.Config.AUDIENCE
import com.example.auth.Config.ISSUER
import com.example.auth.Config.SECRET
import com.example.auth.TokenBlackList
import com.example.auth.TokenBlackList.isTokenBlacklisted
import com.example.auth.generateTokens
import com.example.db.customer.CustomerAddress
import com.example.db.customer.Customer
import com.example.db.customer.CustomerAddressTable
import com.example.db.customer.CustomerTable
import com.example.db.util.DatabaseUtil.dbQuery
import com.example.exceptions.AlreadyExistsException
import com.example.exceptions.PasswordDidNotMatch
import com.example.exceptions.UserDoesNotExist
import com.example.models.customer.*
import com.example.models.customer.address.CustomerAddressResponse
import com.example.models.util.ApprovalStatus
import com.example.models.util.comparePassword
import com.example.models.util.hashedPassword
import org.jetbrains.exposed.sql.*
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

private fun CustomerAddress.toAddressResponse() = CustomerAddressResponse(
    id = this.id.value,
    street = this.street,
    city = this.city,
    state = this.state,
    zip = this.zip
)

private fun Customer.toCustomerResponse() = CustomerResponse(
    id = this.id.value,
    username = this.username,
    firstName = this.firstName,
    lastName = this.lastName,
    phone = this.phone,
    address = this.address.toAddressResponse(),
    gender = this.gender,
    email = this.email,
    password = this.password,
    status = this.status,
    createdAt = Date.from(this.createdAt.atZone(ZoneId.systemDefault()).toInstant()),
    updatedAt = Date.from(this.updatedAt.atZone(ZoneId.systemDefault()).toInstant())
)

suspend fun insertCustomer(customerRequest: CustomerRequest): CustomerResponse = dbQuery {
    val existingCustomer = CustomerTable
        .selectAll().where { CustomerTable.email eq customerRequest.email }
        .singleOrNull()
    if (existingCustomer != null) {
        throw AlreadyExistsException("Customer already exists")
    } else {
        val address = CustomerAddress.new {
            this.street = customerRequest.address.street
            this.city = customerRequest.address.city
            this.state = customerRequest.address.state
            this.zip = customerRequest.address.zip
        }
        return@dbQuery Customer.new {
            this.username = customerRequest.username
            this.firstName = customerRequest.firstName
            this.lastName = customerRequest.lastName
            this.phone = customerRequest.phone
            this.address = CustomerAddress(address.id)
            this.gender = customerRequest.gender
            this.email = customerRequest.email
            this.password = hashedPassword(customerRequest.password)
            this.status = ApprovalStatus.PENDING
            this.createdAt = LocalDateTime.now()
            this.updatedAt = LocalDateTime.now()
        }.toCustomerResponse()
    }
}

suspend fun signInCustomer(email: String, password: String): String? = dbQuery {
    return@dbQuery try {
        Customer.find { CustomerTable.email eq email }
            .singleOrNull()
            ?.takeIf { comparePassword(password, it.password) }
            ?.let { generateTokens(it.email, SECRET, ISSUER, AUDIENCE) }
    } catch (e: Exception) {
        when {
            comparePassword(password, hashedPassword(password)) -> {
                throw PasswordDidNotMatch("The Wrong password")
            }

            else -> {
                throw UserDoesNotExist("No match of customer")
            }
        }
    }
}

suspend fun getCustomerByUsername(username: String): CustomerResponse? = dbQuery {
    return@dbQuery try {
        Customer.find { CustomerTable.username eq username }
            .singleOrNull()
            ?.toCustomerResponse()
    } catch (e: Exception) {
        throw UserDoesNotExist("No such customer")
    }
}

suspend fun signOutCustomer(token: String) = dbQuery {
    if (isTokenBlacklisted(token)) {
        throw UserDoesNotExist("Already logged out")
    } else {
        TokenBlackList.blacklistToken(token)
    }
}

suspend fun getCustomerByStatus(email: String): CustomerResponse? = dbQuery {
    val customerRoleExists = CustomerTable
        .selectAll().where { CustomerTable.email eq email }.count() > 0
    val customerStatus = if (customerRoleExists) {
        Customer.find { CustomerTable.email eq email }
            .singleOrNull()
            ?.toCustomerResponse()
    } else {
        throw UserDoesNotExist("No such customer")
    }
    return@dbQuery customerStatus
}

suspend fun getCustomerByEmail(email: String): CustomerResponse? = dbQuery {
    return@dbQuery try {
        Customer.find { CustomerTable.email eq email }
            .singleOrNull()
            ?.toCustomerResponse()
    } catch (e: Exception) {
        null
    }
}

suspend fun getCustomerById(id: Int): CustomerResponse? = dbQuery {
    return@dbQuery try {
        Customer.findById(id)
            ?.toCustomerResponse()
    } catch (e: Exception) {
        null
    }
}

suspend fun updateCustomerStatus(id: Int, status: ApprovalStatus) = dbQuery {
    return@dbQuery try {
        Customer.findById(id)
            ?.let {
                it.status = status
                it.updatedAt = LocalDateTime.now()
            }
    } catch (e: Exception) {
        null
    }
}

suspend fun updateAllCustomerStatus(status: ApprovalStatus) = dbQuery {
    return@dbQuery try {
        Customer.all()
            .forEach {
                it.status = status
                it.updatedAt = LocalDateTime.now()
            }
    } catch (e: Exception) {
        null
    }
}

suspend fun getAllCustomers(): List<CustomerResponse> = dbQuery {
    return@dbQuery try {
        Customer.all()
            .map { it.toCustomerResponse() }
            .sortedByDescending { it.updatedAt }
    } catch (e: Exception) {
        throw UserDoesNotExist("No customers found")
    }
}

suspend fun updateCustomer(id: Int, customerUpdateRequest: CustomerUpdate): CustomerResponse? = dbQuery {
    return@dbQuery try {
        Customer.findById(id)
            ?.let {
                it.username = customerUpdateRequest.username
                it.firstName = customerUpdateRequest.firstName
                it.lastName = customerUpdateRequest.lastName
                it.phone = customerUpdateRequest.phone
                it.address = CustomerAddress[customerUpdateRequest.address]
                it.gender = customerUpdateRequest.gender
                it.email = customerUpdateRequest.email
                it.password = hashedPassword(customerUpdateRequest.password)
                it.updatedAt = LocalDateTime.now()
                it.toCustomerResponse()
            }
    } catch (e: Exception) {
        null
    }

}

suspend fun deleteCustomer(id: Int): Boolean = dbQuery {
    return@dbQuery try {
        Customer.findById(id)
            ?.delete()
        true
    } catch (e: Exception) {
        false
    }
}