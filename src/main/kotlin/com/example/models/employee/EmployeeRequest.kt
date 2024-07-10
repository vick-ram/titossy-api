package com.example.models.employee

import com.example.exceptions.isPhoneNumber
import com.example.exceptions.password
import com.example.models.util.Gender
import kotlinx.serialization.Serializable
import org.valiktor.functions.*
import org.valiktor.validate

@Serializable
data class EmployeeRequest(
    val username: String,
    val firstName: String,
    val lastName: String,
    val gender: Gender,
    val email: String,
    val password: String,
    val phone: String,
    val role: Roles,
    val availability: Availability? = null
) {
    fun validate(): EmployeeRequest {
        validate(this) {
            validate(EmployeeRequest::username).isNotBlank()
            validate(EmployeeRequest::firstName).isNotBlank()
            validate(EmployeeRequest::lastName).isNotBlank()
            validate(EmployeeRequest::gender).isNotNull().isIn(Gender.entries)
            validate(EmployeeRequest::email).isNotBlank().isEmail()
            validate(EmployeeRequest::password).isNotBlank().password()
            validate(EmployeeRequest::phone).isPhoneNumber()
            validate(EmployeeRequest::role).isNotNull().isIn(Roles.entries)
            validate(EmployeeRequest::availability).isIn(Availability.entries)
        }
        return this
    }
}

enum class Roles {
    ADMIN, MANAGER, INVENTORY, FINANCE, SUPERVISOR, CLEANER
}
enum class Availability {
    AVAILABLE, UNAVAILABLE, ON_LEAVE
}
