package com.example.models.supplier

import com.example.exceptions.password
import kotlinx.serialization.Serializable
import org.valiktor.functions.*
import org.valiktor.validate

@Serializable
data class SupplierSignInData(
    val email: String,
    val password: String
) {
    fun validate(): SupplierSignInData {
        validate(this) {
            validate(SupplierSignInData::email)
                .isEmail()
            validate(SupplierSignInData::password)
                .isNotBlank()
                .password()
        }
        return this
    }
}