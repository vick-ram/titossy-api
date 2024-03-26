package com.example.models.customer.address

import kotlinx.serialization.Serializable
import org.valiktor.functions.isNotBlank
import org.valiktor.validate

@Serializable
data class CustomerAddressRequest(
    val street: String,
    val city: String,
    val state: String,
    val zip: String
){
    init {
        validate(this){
            validate(CustomerAddressRequest::street).isNotBlank()
            validate(CustomerAddressRequest::city).isNotBlank()
            validate(CustomerAddressRequest::state).isNotBlank()
            validate(CustomerAddressRequest::zip).isNotBlank()
        }
    }
}