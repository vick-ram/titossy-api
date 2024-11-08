package com.example.controllers

import com.example.commons.*
import com.example.commons.DatabaseUtil.dbQuery
import com.example.db.Customer
import com.example.db.CustomerTable
import com.example.db.Employee
import com.example.db.EmployeeTable
import com.example.models.EmployeeCredentials
import com.example.models.EmployeeRequest
import com.example.models.EmployeeResponse
import com.example.models.UpdateEmployeeAvailability
import io.ktor.server.plugins.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.selectAll
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import javax.security.auth.login.FailedLoginException

suspend fun createEmployee(
    employeeRequest: EmployeeRequest,
    secret: String,
    scope: CoroutineScope
) = dbQuery {
    val existingEmployee =
        EmployeeTable
            .selectAll()
            .where { EmployeeTable.email eq employeeRequest.email }
            .singleOrNull()

    if (existingEmployee != null) {
        throw AlreadyExistsException("Employee already exists")
    } else {
        val employeeResponse = Employee.new {
            this.username = generateUsername(employeeRequest.firstName)
            this.fullName = "${employeeRequest.firstName} ${employeeRequest.lastName}"
            this.gender = employeeRequest.gender
            this.email = employeeRequest.email
            this.password = hashedPassword(employeeRequest.password, secret)
            this.phone = employeeRequest.phone
            this.role = employeeRequest.role
            this.availability = Availability.AVAILABLE
            this.approvalStatus = ApprovalStatus.APPROVED
            this.createdAt = LocalDateTime.now()
            this.updatedAt = LocalDateTime.now()
            this.tsv =
                "to_tsvector('english, ${this.username} ${this.fullName} ${this.email} ${this.role}')"
        }.toEmployeeResponse()
        val readableDate = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss").format(LocalDateTime.now())
        scope.launch {
            logUserEvent(
                userId = employeeResponse.id,
                event = EventType.REGISTRATION,
                eventData = "Employee ${employeeResponse.fullName} created at $readableDate"
            )
        }
        return@dbQuery employeeResponse
    }
}

suspend fun signInEmployee(
    employeeCredentials: EmployeeCredentials,
    secret: String,
    issuer: String,
    audience: String,
    scope: CoroutineScope
): String = dbQuery {
    return@dbQuery try {
        val employee = Employee.find { EmployeeTable.email.eq(employeeCredentials.email) }?.singleOrNull()

        if (employee != null && !comparePassword(employeeCredentials.password, employee.password, secret)) {
            throw InvalidCredentials("Invalid credentials")
        }
        val readableDate = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss").format(LocalDateTime.now())
        scope.launch {
            logUserEvent(
                userId = employee?.id?.value ?: "",
                event = EventType.LOGIN,
                eventData = "Employee ${employee?.fullName} logged in at $readableDate"
            )
        }

        employee?.let { empl ->
            generateEmployeeToken(
                JwtPayload(
                    sub = empl.id.toString(),
                    email = empl.email,
                    role = empl.role.name,
                    exp = Date(System.currentTimeMillis() + 31_536_000_000),
                    iss = issuer,
                    secret = secret,
                    audience = audience
                )
            )
        } ?: throw NotFoundException("User not found")
    } catch (e: Exception) {
        when (e) {
            is NotFoundException -> throw e
            is InvalidCredentials -> throw e
            else -> throw FailedLoginException("Failed to login employee, try again")
        }
    }
}

suspend fun signOutEmployee(token: String) = dbQuery {
    return@dbQuery try {
        TokenBlackList.blacklistToken(token)
    } catch (e: Exception) {
        when (e) {
            is InvalidToken -> throw e
            is TokenAlreadyBlacklisted -> throw e
            else -> throw UnexpectedError("An unexpected error occurred during sign out")
        }
    }
}

suspend fun updateEmployeeAvailability(
    id: String,
    availability: UpdateEmployeeAvailability
): EmployeeResponse = dbQuery {
    return@dbQuery try {
        Employee.findByIdAndUpdate(id) { update ->
            update.availability = availability.availability
            update.updatedAt = LocalDateTime.now()
        }?.toEmployeeResponse().takeIf { it?.role == Roles.CLEANER }
            ?: throw IllegalArgumentException("Availability is required for cleaner role")
    } catch (e: Exception) {
        when (e) {
            is IllegalArgumentException -> throw e
            else -> throw FailedToCreate("Failed to update employee availability")
        }
    }
}

suspend fun updateEmployee(
    id: String,
    employeeUpdateRequest: EmployeeRequest,
    secret: String,
    scope: CoroutineScope
) = dbQuery {
    val readableDate = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss").format(LocalDateTime.now())
    return@dbQuery try {
        Employee.findByIdAndUpdate(id) { empl ->
            empl.username = generateUsername(employeeUpdateRequest.firstName)
            empl.fullName = "${employeeUpdateRequest.firstName} ${employeeUpdateRequest.lastName}"
            empl.gender = employeeUpdateRequest.gender
            empl.email = employeeUpdateRequest.email
            empl.password = hashedPassword(employeeUpdateRequest.password, secret)
            empl.phone = employeeUpdateRequest.phone
            empl.role = employeeUpdateRequest.role
            empl.availability = employeeUpdateRequest.availability
            empl.updatedAt = LocalDateTime.now()

            scope.launch {
                logUserEvent(
                    userId = empl.id.value,
                    event = EventType.UPDATE,
                    eventData = "Employee ${empl.fullName} updated at $readableDate"
                )
            }
        }
    } catch (e: Exception) {
        when (e) {
            is FailedToCreate -> throw FailedToCreate("Failed to update employee")
            else -> throw e
        }
    }
}

suspend fun deleteEmployee(id: String): Boolean = dbQuery {
    return@dbQuery try {
        val employeeToDelete = Employee.findById(id)
            ?: throw NotFoundException("No id with $id")
        employeeToDelete.delete()
        true
    } catch (e: Exception) {
        throw IllegalArgumentException("Failed to delete the employee with $id")
    }
}

/**
 * Filtered employees
 */
suspend fun filteredEmployees(
    filter: (Employee) -> Boolean
): List<EmployeeResponse>? = dbQuery {
    return@dbQuery try {
        Employee.all()
            .sortedByDescending { it.createdAt.coerceAtLeast(it.updatedAt) }
            .filter { filter(it) && (it.role != Roles.ADMIN) }
            .map { it.toEmployeeResponse() }
            .toList()
    } catch (e: Exception) {
        null
    }
}

suspend fun getAdmin(id: String): EmployeeResponse? = dbQuery {
    return@dbQuery try {
        Employee.findById(id)
            ?.takeIf { it.role == Roles.ADMIN }
            ?.toEmployeeResponse()
    } catch (e: Exception) {
        null
    }
}

suspend fun searchEmployee(query: String): List<EmployeeResponse>? = dbQuery {
    return@dbQuery try {
        EmployeeTable
            .selectAll()
            .where { EmployeeTable.tsv.customMatch(query) }
            .map { Employee.wrapRow(it).toEmployeeResponse() }
            .toList()
    } catch (e: Exception) {
        null
    }
}

suspend fun empForgotPassword(
    changePasswordRequest: ChangePasswordRequest,
    secret: String,
    scope: CoroutineScope
): Boolean = dbQuery {
    return@dbQuery try {
        val employee = Employee.find { EmployeeTable.email.eq(changePasswordRequest.email) }.singleOrNull()
            ?: throw NotFoundException("Employee with email ${changePasswordRequest.email} not found")

        employee.password = hashedPassword(changePasswordRequest.newPassword, secret)
        employee.updatedAt = LocalDateTime.now()

        val readableDate = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss").format(LocalDateTime.now())
        scope.launch {
            logUserEvent(
                userId = employee.id.value,
                event = EventType.PASSWORD_RESET,
                eventData = "Employee ${employee.fullName} password reset at $readableDate"
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


