package com.example.models

import com.example.commons.ApprovalStatus
import com.example.commons.LocalDateTimeSerializer
import com.example.commons.shortUUID
import com.example.commons.isPhoneNumber
import com.example.commons.password
import kotlinx.serialization.Serializable
import org.valiktor.functions.isEmail
import org.valiktor.functions.isNotBlank
import java.time.LocalDateTime

@Serializable
data class SupplierSignInData(
    val email: String,
    val password: String
) {
    fun validate(): SupplierSignInData {
        org.valiktor.validate(this) {
            validate(SupplierSignInData::email)
                .isEmail()
            validate(SupplierSignInData::password)
                .isNotBlank()
                .password()
        }
        return this
    }
}

@Serializable
data class SupplierRequest(
    val firstName: String,
    val lastName: String,
    val company: String,
    val phone: String,
    val address: String,
    val email: String,
    val password: String
) {
    fun validate(): SupplierRequest {
        org.valiktor.validate(this) {
            validate(SupplierRequest::firstName).isNotBlank()
            validate(SupplierRequest::lastName).isNotBlank()
            validate(SupplierRequest::phone).isPhoneNumber()
            validate(SupplierRequest::company).isNotBlank()
            validate(SupplierRequest::address).isNotBlank()
            validate(SupplierRequest::email).isNotBlank().isEmail()
            validate(SupplierRequest::password).isNotBlank().password()
        }
        return this
    }
}

@Serializable
data class SupplierStatusUpdate(
    val status: ApprovalStatus
)

@Serializable
data class SupplierResponse(
    val id: String = shortUUID(),
    val fullName: String,
    val company: String,
    val phone: String,
    val address: String,
    val email: String,
    val password: String,
    val status: ApprovalStatus,
    @Serializable(with = LocalDateTimeSerializer::class)
    val createdAt: LocalDateTime,
    @Serializable(with = LocalDateTimeSerializer::class)
    val updatedAt: LocalDateTime
)