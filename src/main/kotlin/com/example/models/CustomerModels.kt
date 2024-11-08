package com.example.models

import com.example.commons.ApprovalStatus
import com.example.commons.LocalDateTimeSerializer
import com.example.commons.isPhoneNumber
import com.example.commons.password
import kotlinx.serialization.Serializable
import org.valiktor.functions.isEmail
import org.valiktor.functions.isNotBlank
import org.valiktor.functions.isNotEmpty
import org.valiktor.functions.isNotNull
import java.time.LocalDateTime

@Serializable
data class CustomerRequest(
    val firstName: String,
    val lastName: String,
    val phone: String,
    val email: String,
    val password: String
) {
    fun validate(): CustomerRequest {
        org.valiktor.validate(this) {
            validate(CustomerRequest::firstName).isNotBlank()
            validate(CustomerRequest::lastName).isNotBlank()
            validate(CustomerRequest::phone).isPhoneNumber()
            validate(CustomerRequest::email).isNotBlank().isEmail()
            validate(CustomerRequest::password).isNotEmpty().password()
        }
        return this
    }
}

@Serializable
data class CustomerSignInData(
    val email: String,
    val password: String
) {
    fun validate(): CustomerSignInData {
        org.valiktor.validate(this) {
            validate(CustomerSignInData::email)
                .isEmail()
            validate(CustomerSignInData::password)
                .password()
        }
        return this
    }
}

@Serializable
data class StatusUpdate(
    val status: ApprovalStatus
) {
    fun validate(): StatusUpdate {
        org.valiktor.validate(this) {
            validate(StatusUpdate::status).isNotNull()
        }
        return this
    }
}

@Serializable
data class CustomerResponse(
    val id: String,
    val fullName: String,
    val phone: String,
    val address: String?,
    val email: String,
    val password: String,
    val status: ApprovalStatus,
    @Serializable(with = LocalDateTimeSerializer::class)
    val createdAt: LocalDateTime,
    @Serializable(with = LocalDateTimeSerializer::class)
    val updatedAt: LocalDateTime
)