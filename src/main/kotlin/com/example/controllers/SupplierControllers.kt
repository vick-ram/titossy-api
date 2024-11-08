package com.example.controllers

import com.example.commons.*
import com.example.commons.DatabaseUtil.dbQuery
import com.example.db.Supplier
import com.example.db.SupplierTable
import com.example.models.SupplierRequest
import com.example.models.SupplierResponse
import com.example.models.SupplierSignInData
import com.example.models.SupplierStatusUpdate
import io.ktor.server.plugins.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.jetbrains.exposed.sql.selectAll
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

suspend fun createSupplier(
    supplierRequest: SupplierRequest,
    secret: String,
    scope: CoroutineScope
): SupplierResponse = dbQuery {
    val supplierExists = SupplierTable
        .selectAll()
        .where { SupplierTable.email eq supplierRequest.email }
        .count() > 0
    if (supplierExists) {
        throw AlreadyExistsException("Supplier already exists")
    } else {
        val newSupplier = Supplier.new {
            this.fullName = "${supplierRequest.firstName} ${supplierRequest.lastName}"
            this.company = supplierRequest.company
            this.phone = supplierRequest.phone
            this.address = supplierRequest.address
            this.email = supplierRequest.email
            this.password = hashedPassword(supplierRequest.password, secret)
            this.status = ApprovalStatus.PENDING
            this.createdAt = LocalDateTime.now()
            this.updatedAt = LocalDateTime.now()
            this.tsv = "to_tsvector('english, ${this.fullName} ${this.email}')"
        }
        val readableDate = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss").format(LocalDateTime.now())
        scope.launch {
            logUserEvent(
                userId = newSupplier.id.value,
                event = EventType.REGISTRATION,
                eventData = "Supplier ${newSupplier.fullName} created at $readableDate"
            )
        }
        return@dbQuery newSupplier.toSupplierResponse()
    }
}

suspend fun signInSupplier(
    credentials: SupplierSignInData,
    secret: String,
    issuer: String,
    audience: String,
    scope: CoroutineScope
): String = dbQuery {
    return@dbQuery try {
        val supplier = Supplier
            .find { SupplierTable.email eq credentials.email }
            .singleOrNull()

        if (supplier != null && !comparePassword(credentials.password, supplier.password, secret)) {
            throw InvalidCredentials("Wrong password provided for ${credentials.email}")
        }
        val readableDate = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss").format(LocalDateTime.now())

        scope.launch {
            logUserEvent(
                userId = supplier?.id?.value ?: "",
                event = EventType.LOGIN,
                eventData = "Supplier ${supplier?.fullName} logged in at $readableDate"
            )
        }
        supplier?.let { cust ->
            generateToken(
                JwtPayload(
                    sub = cust.id.toString(),
                    email = cust.email,
                    exp = Date(System.currentTimeMillis() + 31_536_000_000),
                    iss = issuer,
                    secret = secret,
                    audience = audience
                )
            )
        } ?: throw NotFoundException("User with ${credentials.email} not found")
    } catch (e: Exception) {
        when (e) {
            is NotFoundException -> throw e
            is InvalidCredentials -> throw e
            else -> throw UnexpectedError("An unexpected error occurred")
        }
    }
}

suspend fun signOutSupplier(token: String): Boolean = dbQuery {
    return@dbQuery try {
        TokenBlackList.blacklistToken(token)
        true
    } catch (e: Exception) {
        throw NoRecordFoundException("No match of supplier")
    }
}

suspend fun updateSupplierStatus(
    id: String,
    status: SupplierStatusUpdate,
    scope: CoroutineScope
) = dbQuery {
    return@dbQuery try {
        val readableDate = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss").format(LocalDateTime.now())
        Supplier.findById(id)
            ?.let {
                it.status = status.status
                it.updatedAt = LocalDateTime.now()
                scope.launch {
                    logUserEvent(
                        userId = id,
                        event = EventType.UPDATE,
                        eventData = "Supplier's status updated from ${it.status} to ${status.status} at $readableDate"
                    )
                }
            }
    } catch (e: Exception) {
        throw NoRecordFoundException("No match of supplier")
    }
}

suspend fun updateAllSuppliersStatus(
    updateStatus: SupplierStatusUpdate
) = dbQuery {
    return@dbQuery try {
        Supplier.all()
            .forEach {
                it.status = updateStatus.status
                it.updatedAt = LocalDateTime.now()
            }.takeIf { updateStatus.status == ApprovalStatus.PENDING }
    } catch (e: Exception) {
        throw NoRecordFoundException("No match of supplier")
    }
}

suspend fun updateSupplier(
    id: String,
    supplierUpdateRequest: SupplierRequest,
    secret: String
): Boolean = dbQuery {
    return@dbQuery try {
        Supplier.findByIdAndUpdate(id) { supplierUpdate ->
            supplierUpdate.fullName = "${supplierUpdateRequest.firstName} ${supplierUpdateRequest.lastName}"
            supplierUpdate.address = supplierUpdateRequest.address
            supplierUpdate.phone = supplierUpdateRequest.phone
            supplierUpdate.email = supplierUpdateRequest.email
            supplierUpdate.password = hashedPassword(supplierUpdateRequest.password, secret)
            supplierUpdate.updatedAt = LocalDateTime.now()
        }
        true
    } catch (e: Exception) {
        throw NoRecordFoundException("No match of supplier")
    }
}

suspend fun deleteSupplier(id: String): Boolean = dbQuery {
    return@dbQuery try {
        Supplier.findById(id)
            ?.delete()
        true
    } catch (e: Exception) {
        throw NoRecordFoundException("No match of supplier")
    }
}

suspend fun filteredSuppliers(
    filter: (Supplier) -> Boolean
): List<SupplierResponse>? = dbQuery {
    return@dbQuery try {
        Supplier.all()
            .sortedByDescending { it.createdAt.coerceAtLeast(it.updatedAt) }
            .filter(filter)
            .map { it.toSupplierResponse() }
            .toList()
    } catch (e: Exception) {
        null
    }
}

suspend fun searchSupplier(query: String): List<SupplierResponse>? = dbQuery {
    return@dbQuery try {
        SupplierTable
            .selectAll()
            .where { SupplierTable.tsv.customMatch(query) }
            .map { Supplier.wrapRow(it).toSupplierResponse() }
            .toList()
    } catch (e: Exception) {
        null
    }
}

suspend fun supForgotPassword(
    changePasswordRequest: ChangePasswordRequest,
    secret: String,
    scope: CoroutineScope
): Boolean = dbQuery {
    return@dbQuery try {
        val supplier = Supplier.find { SupplierTable.email.eq(changePasswordRequest.email) }.singleOrNull()
            ?: throw NotFoundException("Supplier with email ${changePasswordRequest.email} not found")

        supplier.password = hashedPassword(changePasswordRequest.newPassword, secret)
        supplier.updatedAt = LocalDateTime.now()

        val readableDate = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss").format(LocalDateTime.now())
        scope.launch {
            logUserEvent(
                userId = supplier.id.value,
                event = EventType.PASSWORD_RESET,
                eventData = "Supplier ${supplier.fullName} password reset at $readableDate"
            )
        }
        true
    } catch (e: Exception) {
        when (e) {
            is NotFoundException -> throw e
            else -> throw UnexpectedError("An unexpected error occurred")
        }
    }
}