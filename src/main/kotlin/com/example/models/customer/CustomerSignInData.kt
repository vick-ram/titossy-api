package com.example.models.customer

import com.example.models.util.ApprovalStatus
import kotlinx.serialization.Serializable
import org.valiktor.functions.*
import org.valiktor.validate

@Serializable
data class CustomerSignInData(
    val email: String,
    val password: String
) {
    init {
        validate(this) {
            validate(CustomerSignInData::email).isNotBlank().isEmail()
            validate(CustomerSignInData::password)
                .matches(Regex("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}\$"))
        }
    }
}

@Serializable
data class StatusUpdate(
    val status: ApprovalStatus
) {
    init {
        validate(this) {
            validate(StatusUpdate::status).isNotNull()
        }
    }
}
