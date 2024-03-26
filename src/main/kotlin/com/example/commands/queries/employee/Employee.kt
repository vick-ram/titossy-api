package com.example.commands.queries.employee

import com.example.auth.Config.AUDIENCE
import com.example.auth.Config.ISSUER
import com.example.auth.Config.SECRET
import com.example.auth.TokenBlackList
import com.example.auth.generateEmployeeTokens
import com.example.auth.generateTokens
import com.example.db.employee.Employee
import com.example.db.employee.EmployeeTable
import com.example.db.util.DatabaseUtil.dbQuery
import com.example.exceptions.PasswordDidNotMatch
import com.example.exceptions.UserDoesNotExist
import com.example.models.employee.EmployeeRequest
import com.example.models.employee.EmployeeResponse
import com.example.models.employee.EmployeeUpdate
import com.example.models.employee.Roles
import com.example.models.util.hashedPassword
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

private fun Employee.toEmployeeResponse() = EmployeeResponse(
    id = this.id.value,
    username = this.username,
    firstName = this.firstName,
    lastName = this.lastName,
    gender = this.gender,
    email = this.email,
    password = this.password,
    phone = this.phone,
    role = this.role,
    availability = this.availability,
    createdAt = Date.from(this.createdAt.atZone(ZoneId.systemDefault()).toInstant()),
    updatedAt = Date.from(this.updatedAt.atZone(ZoneId.systemDefault()).toInstant())
)

suspend fun createEmployee(employeeRequest: EmployeeRequest) = dbQuery {
    val existingEmployee =
        EmployeeTable
            .selectAll().where { EmployeeTable.email eq employeeRequest.email }
            .singleOrNull()

    if (existingEmployee != null) {
        return@dbQuery null
    } else {
        val employeeResponse = Employee.new {
            username = employeeRequest.username
            firstName = employeeRequest.firstName
            lastName = employeeRequest.lastName
            gender = employeeRequest.gender
            email = employeeRequest.email
            password = hashedPassword(employeeRequest.password)
            phone = employeeRequest.phone
            role = employeeRequest.role
            availability = employeeRequest.availability
            createdAt = LocalDateTime.now()
            updatedAt = LocalDateTime.now()
        }.toEmployeeResponse()
        return@dbQuery employeeResponse
    }
}

suspend fun signInEmployee(email: String, password: String): String? = dbQuery {
    return@dbQuery try {
        Employee.find { EmployeeTable.email eq email }
            .singleOrNull()
            ?.takeIf { it.password == hashedPassword(password) }
            ?.let { generateEmployeeTokens(it.email, SECRET, ISSUER, AUDIENCE, it.role.name) }
    } catch (e: Exception) {
        throw UserDoesNotExist("User does not exist")
    }
}

suspend fun signOutEmployee(token: String) = dbQuery {
    TokenBlackList.blacklistToken(token)
}

suspend fun getEmployeeById(id: Int): EmployeeResponse? = dbQuery {
    return@dbQuery try {
        Employee.find { EmployeeTable.id eq id }
            .singleOrNull()
            ?.toEmployeeResponse()
    } catch (e: Exception) {
        throw UserDoesNotExist("User does not exist")
    }
}

suspend fun getEmployeeByEmail(email: String): EmployeeResponse? = dbQuery {
    return@dbQuery try {
        Employee.find { EmployeeTable.email eq email }
            .singleOrNull()?.toEmployeeResponse()
    } catch (e: Exception) {
        throw UserDoesNotExist("User does not exist")
    }
}

suspend fun getEmployeeByRole(role: Roles): EmployeeResponse? = dbQuery {
    return@dbQuery try {
        Employee.find { EmployeeTable.role eq role }
            .singleOrNull()
            ?.toEmployeeResponse()
    } catch (e: Exception) {
        throw UserDoesNotExist("No employee with role $role found")
    }
}

suspend fun getAllEmployees(): List<EmployeeResponse> = dbQuery {
    return@dbQuery try {
        Employee.all()
            .map { it.toEmployeeResponse() }
    } catch (e: Exception) {
        throw UserDoesNotExist("No employees found")
    }
}

suspend fun updateEmployee(id: Int, employeeUpdateRequest: EmployeeUpdate) = dbQuery {
    return@dbQuery try {
        val employee = Employee.findById(id)
        employee?.apply {
            this.username = employeeUpdateRequest.username
            this.firstName = employeeUpdateRequest.firstName
            this.lastName = employeeUpdateRequest.lastName
            this.gender = employeeUpdateRequest.gender
            this.email = employeeUpdateRequest.email
            this.password = hashedPassword(employeeUpdateRequest.password)
            this.phone = employeeUpdateRequest.phone
            this.role = employeeUpdateRequest.role
            this.availability = employeeUpdateRequest.availability
            this.updatedAt = LocalDateTime.now()
        }
    }catch (e: Exception){
        return@dbQuery null
    }
}

suspend fun deleteEmployee(id: Int): Boolean = dbQuery {
    return@dbQuery try {
        Employee.findById(id)
            ?.delete()
        true
    } catch (e: Exception) {
        return@dbQuery false
    }
}


