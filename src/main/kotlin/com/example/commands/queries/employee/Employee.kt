package com.example.commands.queries.employee

import com.example.auth.Config.AUDIENCE
import com.example.auth.Config.ISSUER
import com.example.auth.Config.SECRET
import com.example.auth.JwtPayload
import com.example.auth.TokenBlackList
import com.example.auth.generateEmployeeTokens
import com.example.commands.customMatch
import com.example.db.employee.Employee
import com.example.db.employee.EmployeeTable
import com.example.db.util.DatabaseUtil.dbQuery
import com.example.exceptions.*
import com.example.models.employee.*
import com.example.models.util.ApprovalStatus
import com.example.models.util.comparePassword
import com.example.models.util.hashedPassword
import io.ktor.server.plugins.*
import org.jetbrains.exposed.sql.selectAll
import java.time.LocalDateTime
import java.util.*
import javax.security.auth.login.FailedLoginException

suspend fun createEmployee(employeeRequest: EmployeeRequest) = dbQuery {
    val existingEmployee =
        EmployeeTable
            .selectAll().where { EmployeeTable.email eq employeeRequest.email }
            .singleOrNull()

    if (existingEmployee != null) {
        throw AlreadyExistsException("Employee already exists")
    } else {
        val employeeResponse = Employee.new {
            username = employeeRequest.username
            fullName = "${employeeRequest.firstName} ${employeeRequest.lastName}"
            this.gender = employeeRequest.gender
            this.email = employeeRequest.email
            this.password = hashedPassword(employeeRequest.password)
            this.phone = employeeRequest.phone
            this.role = employeeRequest.role
            this.availability = Availability.AVAILABLE
            this.approvalStatus = ApprovalStatus.APPROVED
            this.createdAt = LocalDateTime.now()
            this.updatedAt = LocalDateTime.now()
            this.tsv =
                "to_tsvector('english, ${this.username} ${this.fullName} ${this.email} ${this.role}')"
        }.toEmployeeResponse()
        return@dbQuery employeeResponse
    }
}

suspend fun signInEmployee(employeeCredentials: EmployeeCredentials): String = dbQuery {
    return@dbQuery try {

        val employee = when {
            employeeCredentials.email != null -> Employee.find { EmployeeTable.email eq employeeCredentials.email }
            employeeCredentials.username != null -> Employee.find { EmployeeTable.username eq employeeCredentials.username }
            else -> null
        }?.singleOrNull()

        if (employee != null && !comparePassword(employeeCredentials.password, employee.password)) {
            throw InvalidCredentials("Invalid credentials")
        }

        employee?.let { empl ->
            generateEmployeeTokens(
                JwtPayload(
                    sub = empl.id.toString(),
                    email = empl.email,
                    username = empl.username,
                    role = empl.role.name,
                    exp = Date(System.currentTimeMillis() + 31_536_000_000),
                    iss = ISSUER,
                    secret = SECRET,
                    audience = AUDIENCE
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

suspend fun updateEmployeeAvailability(id: UUID, availability: UpdateEmployeeAvailability): EmployeeResponse = dbQuery {
    return@dbQuery try {
        Employee.findByIdAndUpdate(id) {
            it.availability = availability.availability
            it.updatedAt = LocalDateTime.now()
        }?.toEmployeeResponse().takeIf { it?.role == Roles.CLEANER }
            ?: throw IllegalArgumentException("Availability is required for cleaner role")
    } catch (e: Exception) {
        when (e) {
            is IllegalArgumentException -> throw e
            else -> throw FailedToCreate("Failed to update employee availability")
        }
    }
}

suspend fun updateEmployee(id: UUID, employeeUpdateRequest: EmployeeRequest) = dbQuery {
    return@dbQuery try {
        Employee.findByIdAndUpdate(id) { empl ->
            empl.username = employeeUpdateRequest.username
            empl.fullName = "${employeeUpdateRequest.firstName} ${employeeUpdateRequest.lastName}"
            empl.gender = employeeUpdateRequest.gender
            empl.email = employeeUpdateRequest.email
            empl.password = hashedPassword(employeeUpdateRequest.password)
            empl.phone = employeeUpdateRequest.phone
            empl.role = employeeUpdateRequest.role
            empl.availability = employeeUpdateRequest.availability
            empl.updatedAt = LocalDateTime.now()
        }
    } catch (e: Exception) {
        when (e) {
            is FailedToCreate -> throw FailedToCreate("Failed to update employee")
            else -> throw e
        }
    }
}

suspend fun deleteEmployee(id: UUID): Boolean = dbQuery {
    return@dbQuery try {
        val employeeToDelete = Employee.findById(id) ?: throw NotFoundException("No id with $id")
        employeeToDelete.delete()
        true
    } catch (e: Exception) {
        throw IllegalArgumentException("Failed to delete the employee with $id")
    }
}

/**
 * Filtered employees
 */
suspend fun filteredEmployees(filter: (Employee) -> Boolean): List<EmployeeResponse>? = dbQuery {
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

/**
 * Search employee
 */
suspend fun searchEmployee(query: String): List<EmployeeResponse>? = dbQuery {
    return@dbQuery try {
        EmployeeTable.selectAll()
            .where { EmployeeTable.tsv.customMatch(query) }
            .map { Employee.wrapRow(it).toEmployeeResponse() }
            .toList()
    } catch (e: Exception) {
        null
    }
}

