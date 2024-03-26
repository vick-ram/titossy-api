package com.example.models.customer

import com.example.models.customer.address.CustomerAddressRequest
import kotlinx.serialization.Serializable
import org.valiktor.functions.*
import org.valiktor.validate

@Serializable
data class CustomerRequest(
    val username: String,
    val firstName: String,
    val lastName: String,
    val phone: Long,
    val address: CustomerAddressRequest,
    val gender: Gender,
    val email: String,
    val password: String
) {
    init {
        validate(this) {
            validate(CustomerRequest::username).isNotBlank()
            validate(CustomerRequest::firstName).isNotBlank()
            validate(CustomerRequest::lastName).isNotBlank()
            validate(CustomerRequest::email).isNotBlank().isEmail()
            validate(CustomerRequest::password).isNotEmpty()
                .matches(Regex("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}\$"))
        }
    }
}

enum class Gender {
    MALE, FEMALE, OTHER, NOT_SPECIFIED
}