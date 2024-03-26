package com.example.models.supplier.address

import kotlinx.serialization.Serializable
import org.valiktor.functions.isNotBlank
import org.valiktor.validate

@Serializable
data class SupplierAddressRequest(
    val street: String,
    val city: String,
    val state: String,
    val zip: String
){
    init {
        validate(this){
            validate(SupplierAddressRequest::street).isNotBlank()
            validate(SupplierAddressRequest::city).isNotBlank()
            validate(SupplierAddressRequest::state).isNotBlank()
            validate(SupplierAddressRequest::zip).isNotBlank()
        }
    }
}