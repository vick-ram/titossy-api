package com.example.models.employee

import kotlinx.serialization.Serializable
import org.valiktor.functions.*
import org.valiktor.validate

@Serializable
data class EmployeeRequest(
    val username: String,
    val firstName: String,
    val lastName: String,
    val gender: String,
    val email: String,
    val password: String,
    val phone: Long,
    val role: Roles,
    val availability: Availability?
) {
    init {
        validate(this) {
            validate(EmployeeRequest::username).isNotBlank()
            validate(EmployeeRequest::firstName).isNotBlank()
            validate(EmployeeRequest::lastName).isNotBlank()
            validate(EmployeeRequest::gender).isNotBlank()
            validate(EmployeeRequest::email).isNotBlank().isEmail()
            validate(EmployeeRequest::password)
                .isNotBlank().matches(Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+\$"))
            validate(EmployeeRequest::phone).isPositive().hasDigits(10)
        }
    }
}

enum class Roles {
    ADMIN, MANAGER, INVENTORY, FINANCE, SUPERVISOR, CLEANER
}
enum class Availability {
    AVAILABLE, UNAVAILABLE, ON_LEAVE
}
