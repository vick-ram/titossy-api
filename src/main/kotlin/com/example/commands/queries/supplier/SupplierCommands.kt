package com.example.commands.queries.supplier

import com.example.auth.Config.AUDIENCE
import com.example.auth.Config.ISSUER
import com.example.auth.Config.SECRET
import com.example.auth.JwtPayload
import com.example.auth.TokenBlackList
import com.example.auth.generateTokens
import com.example.commands.customMatch
import com.example.db.supplier.Supplier
import com.example.db.supplier.SupplierTable
import com.example.db.util.DatabaseUtil.dbQuery
import com.example.exceptions.AlreadyExistsException
import com.example.exceptions.InvalidCredentials
import com.example.exceptions.NoRecordFoundException
import com.example.exceptions.UnexpectedError
import com.example.models.supplier.SupplierRequest
import com.example.models.supplier.SupplierResponse
import com.example.models.supplier.SupplierSignInData
import com.example.models.supplier.SupplierStatusUpdate
import com.example.models.util.ApprovalStatus
import com.example.models.util.comparePassword
import com.example.models.util.hashedPassword
import io.ktor.server.plugins.*
import org.jetbrains.exposed.sql.selectAll
import java.time.LocalDateTime
import java.util.*

suspend fun createSupplier(supplierRequest: SupplierRequest): SupplierResponse = dbQuery {
    val supplierExists = SupplierTable
        .selectAll()
        .where { SupplierTable.email eq supplierRequest.email }
        .count() > 0
    if (supplierExists) {
        throw AlreadyExistsException("Supplier already exists")
    } else {
        return@dbQuery Supplier.new {
            this.fullName = "${supplierRequest.firstName} ${supplierRequest.lastName}"
            this.phone = supplierRequest.phone
            this.address = supplierRequest.address
            this.email = supplierRequest.email
            this.password = hashedPassword(supplierRequest.password)
            this.status = ApprovalStatus.PENDING
            this.createdAt = LocalDateTime.now()
            this.updatedAt = LocalDateTime.now()
            this.tsv = "to_tsvector('english, ${this.fullName} ${this.email}')"
        }.toSupplierResponse()
    }
}

suspend fun signInSupplier(credentials: SupplierSignInData): String = dbQuery {
    return@dbQuery try {
        val supplier = Supplier.find { SupplierTable.email eq credentials.email }.singleOrNull()

        if (supplier != null && !comparePassword(credentials.password, supplier.password)) {
            throw InvalidCredentials("Wrong password provided for ${credentials.email}")
        }


        supplier?.let { cust ->
            generateTokens(
                JwtPayload(
                    sub = cust.id.toString(),
                    email = cust.email,
                    username = null,
                    exp = Date(System.currentTimeMillis() + 31_536_000_000),
                    iss = ISSUER,
                    secret = SECRET,
                    audience = AUDIENCE
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
    id: UUID,
    status: SupplierStatusUpdate
) = dbQuery {
    return@dbQuery try {
        Supplier.findById(id)
            ?.let {
                it.status = status.status
                it.updatedAt = LocalDateTime.now()
            }
    } catch (e: Exception) {
        throw NoRecordFoundException("No match of supplier")
    }
}

suspend fun updateAllSuppliersStatus(updateStatus: SupplierStatusUpdate) = dbQuery {
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

suspend fun updateSupplier(id: UUID, supplierUpdateRequest: SupplierRequest): Boolean = dbQuery {
    return@dbQuery try {
        Supplier.findById(id)
            ?.let {
                it.fullName = "${supplierUpdateRequest.firstName} ${supplierUpdateRequest.lastName}"
                it.phone = supplierUpdateRequest.phone
                it.address = supplierUpdateRequest.address
                it.email = supplierUpdateRequest.email
                it.password = hashedPassword(supplierUpdateRequest.password)
                it.updatedAt = LocalDateTime.now()
            }
        true
    } catch (e: Exception) {
        throw NoRecordFoundException("No match of supplier")
    }
}

suspend fun deleteSupplier(id: UUID): Boolean = dbQuery {
    return@dbQuery try {
        Supplier.findById(id)
            ?.delete()
        true
    } catch (e: Exception) {
        throw NoRecordFoundException("No match of supplier")
    }
}

suspend fun filteredSuppliers(filter: (Supplier) -> Boolean): List<SupplierResponse>? = dbQuery {
    return@dbQuery try {
        Supplier.all()
            .limit(50)
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
