package com.example.models.customer

import com.example.exceptions.isPhoneNumber
import com.example.exceptions.password
import com.example.models.util.Gender
import kotlinx.serialization.Serializable
import org.valiktor.functions.*
import org.valiktor.validate

@Serializable
data class CustomerRequest(
    val username: String,
    val firstName: String,
    val lastName: String,
    val phone: String,
    val email: String,
    val password: String
) {
    fun validate(): CustomerRequest {
        validate(this) {
            validate(CustomerRequest::username).isNotBlank()
            validate(CustomerRequest::firstName).isNotBlank()
            validate(CustomerRequest::lastName).isNotBlank()
            validate(CustomerRequest::phone).isPhoneNumber()
            validate(CustomerRequest::email).isNotBlank().isEmail()
            validate(CustomerRequest::password).isNotEmpty().password()
        }
        return this
    }
}


