package com.example.models.customer

import com.example.exceptions.password
import com.example.models.util.ApprovalStatus
import kotlinx.serialization.Serializable
import org.valiktor.functions.*
import org.valiktor.validate

@Serializable
data class CustomerSignInData(
    val email: String? = null,
    val username: String? = null,
    val password: String
) {
    fun validate(): CustomerSignInData {
        validate(this) {
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
        validate(this) {
            validate(StatusUpdate::status).isNotNull()
        }
        return this
    }
}
