package com.example.models.employee

import kotlinx.serialization.Serializable
import org.valiktor.functions.*
import org.valiktor.validate

@Serializable
data class EmployeeCredentials(
    val email: String,
    val password: String
){
    init {
        validate(this){
            validate(EmployeeCredentials::email).isNotBlank().isEmail()
            validate(EmployeeCredentials::password)
                .isNotBlank()
                .hasSize(8, 20)
                .matches(Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+\$"))
        }
    }
}
