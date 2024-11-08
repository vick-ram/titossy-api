package com.example.models

import com.example.commons.*
import com.example.commons.isPhoneNumber
import com.example.commons.password
import kotlinx.serialization.Serializable
import org.valiktor.functions.isEmail
import org.valiktor.functions.isIn
import org.valiktor.functions.isNotBlank
import org.valiktor.functions.isNotNull
import java.time.LocalDateTime

@Serializable
data class EmployeeCredentials(
    val email: String,
    val password: String
) {
    fun validate(): EmployeeCredentials {
        org.valiktor.validate(this) {
            validate(EmployeeCredentials::email)
                .isEmail()
            validate(EmployeeCredentials::password).password()
        }
        return this
    }
}

@Serializable
data class EmployeeRequest(
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
        org.valiktor.validate(this) {
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

@Serializable
data class UpdateEmployeeAvailability(
    val availability: Availability
)

@Serializable
data class EmployeeResponse(
    val id: String = shortUUID(),
    val username: String,
    val fullName: String,
    val gender: Gender,
    val email: String,
    val password: String,
    val phone: String,
    val role: Roles,
    val availability: Availability?,
    val approvalStatus: ApprovalStatus?,
    @Serializable(with = LocalDateTimeSerializer::class)
    val createdAt: LocalDateTime,
    @Serializable(with = LocalDateTimeSerializer::class)
    val updatedAt: LocalDateTime
)