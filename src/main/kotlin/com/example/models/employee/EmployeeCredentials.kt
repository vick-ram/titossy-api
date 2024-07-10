package com.example.models.employee

import com.example.exceptions.password
import kotlinx.serialization.Serializable
import org.valiktor.functions.*
import org.valiktor.validate

@Serializable
data class EmployeeCredentials(
    val email: String? = null,
    val username: String? = null,
    val password: String
) {
    fun validate(): EmployeeCredentials {
        validate(this) {
            validate(EmployeeCredentials::email)
                .isEmail()
            validate(EmployeeCredentials::username)
            validate(EmployeeCredentials::password).password()
        }
        return this
    }
}
