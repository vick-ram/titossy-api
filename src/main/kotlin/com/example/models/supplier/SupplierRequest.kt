package com.example.models.supplier

import com.example.exceptions.isPhoneNumber
import com.example.exceptions.password
import kotlinx.serialization.Serializable
import org.valiktor.functions.*
import org.valiktor.validate

@Serializable
data class SupplierRequest(
    val firstName: String,
    val lastName: String,
    val phone: String,
    val address: String,
    val email: String,
    val password: String
) {
    fun validate(): SupplierRequest {
        validate(this) {
            validate(SupplierRequest::firstName).isNotBlank()
            validate(SupplierRequest::lastName).isNotBlank()
            validate(SupplierRequest::phone).isPhoneNumber()
            validate(SupplierRequest::address).isNotBlank()
            validate(SupplierRequest::email).isNotBlank().isEmail()
            validate(SupplierRequest::password).isNotBlank().password()
        }
        return this
    }
}