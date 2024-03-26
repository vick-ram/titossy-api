package com.example.models.supplier

import com.example.models.supplier.address.SupplierAddressRequest
import kotlinx.serialization.Serializable
import org.valiktor.functions.*
import org.valiktor.validate

@Serializable
data class SupplierRequest(
    val username: String,
    val firstName: String,
    val lastName: String,
    val phone: Long,
    val address: SupplierAddressRequest,
    val email: String,
    val password: String
) {
    init {
        validate(this) {
            validate(SupplierRequest::username).isNotBlank()
            validate(SupplierRequest::firstName).isNotBlank()
            validate(SupplierRequest::lastName).isNotBlank()
            validate(SupplierRequest::phone).isPositive()
            validate(SupplierRequest::email).isNotBlank().isEmail()
            validate(SupplierRequest::password)
                .isNotBlank()
                .matches(Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+\$")
            )
        }
    }
}