package com.example.models.supplier

import kotlinx.serialization.Serializable
import org.valiktor.functions.*
import org.valiktor.validate

@Serializable
data class SupplierSignInData(
    val email: String,
    val password: String
) {
    init {
        validate(this){
            validate(SupplierSignInData::email)
                .isNotBlank()
                .isEmail()
            validate(SupplierSignInData::password)
                .isNotBlank()
                .matches(Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+\$"))
        }
    }
}