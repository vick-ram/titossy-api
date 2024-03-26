package com.example.commands.queries.supplier

import com.example.auth.Config.AUDIENCE
import com.example.auth.Config.ISSUER
import com.example.auth.Config.SECRET
import com.example.auth.TokenBlackList
import com.example.auth.generateTokens
import com.example.db.supplier.Supplier
import com.example.db.supplier.SupplierAddress
import com.example.db.supplier.SupplierTable
import com.example.db.util.DatabaseUtil.dbQuery
import com.example.models.util.ApprovalStatus
import com.example.models.supplier.SupplierRequest
import com.example.models.supplier.SupplierResponse
import com.example.models.supplier.SupplierUpdate
import com.example.models.supplier.address.SupplierAddressResponse
import com.example.models.util.hashedPassword
import org.jetbrains.exposed.sql.selectAll
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

private fun SupplierAddress.toSupplierAddressResponse() = SupplierAddressResponse(
    id = this.id.value,
    street = this.street,
    city = this.city,
    state = this.state,
    zip = this.zip
)

private fun Supplier.toSupplierResponse() = SupplierResponse(
    id = this.id.value,
    username = this.username,
    firstName = this.firstName,
    lastName = this.lastName,
    phone = this.phone,
    address = this.address.toSupplierAddressResponse(),
    email = this.email,
    password = this.password,
    status = this.status,
    createdAt = Date.from(this.createdAt.atZone(ZoneId.systemDefault()).toInstant()),
    updatedAt = Date.from(this.updatedAt.atZone(ZoneId.systemDefault()).toInstant())
)

suspend fun createSupplier(supplierRequest: SupplierRequest): SupplierResponse = dbQuery {
    val existingSupplier = SupplierTable
        .selectAll().where { SupplierTable.email eq supplierRequest.email }
        .singleOrNull()

    if (existingSupplier != null) {
        throw Exception("Supplier already exists")
    } else {
        val address = SupplierAddress.new {
            this.street = supplierRequest.address.street
            this.city = supplierRequest.address.city
            this.state = supplierRequest.address.state
            this.zip = supplierRequest.address.zip
        }
        Supplier.new {
            this.username = supplierRequest.username
            this.firstName = supplierRequest.firstName
            this.lastName = supplierRequest.lastName
            this.phone = supplierRequest.phone
            this.address = SupplierAddress(address.id)
            this.email = supplierRequest.email
            this.password = hashedPassword(supplierRequest.password)
            this.status = ApprovalStatus.PENDING
            this.createdAt = LocalDateTime.now()
            this.updatedAt = LocalDateTime.now()
        }.toSupplierResponse()
    }
}

suspend fun signInSupplier(email: String, password: String): String? = dbQuery {
    return@dbQuery try {
        Supplier.find { SupplierTable.email eq email }
            .singleOrNull()
            ?.takeIf { it.password == hashedPassword(password) }
            ?.let { generateTokens(it.email, SECRET, ISSUER, AUDIENCE) }
    } catch (e: Exception) {
        throw e
    }
}

suspend fun getSupplierByEmail(email: String) = dbQuery {
    return@dbQuery try {
        Supplier.find { SupplierTable.email eq email }
            .singleOrNull()
            ?.toSupplierResponse()
    } catch (e: Exception) {
        throw e
    }
}

suspend fun signOutSupplier(token: String): Boolean = dbQuery {
    return@dbQuery try {
        TokenBlackList.blacklistToken(token)
        true
    } catch (e: Exception) {
        throw e
    }
}

suspend fun getSupplierByStatus(email: String): SupplierResponse? = dbQuery {
    return@dbQuery try {
        Supplier.find { SupplierTable.email eq email }
            .singleOrNull()
            ?.toSupplierResponse()
    } catch (e: Exception) {
        throw e
    }
}

suspend fun getSupplierById(id: Int): SupplierResponse? = dbQuery {
    return@dbQuery try {
        Supplier.findById(id)
            ?.toSupplierResponse()
    } catch (e: Exception) {
        throw e
    }
}

suspend fun updateSupplierStatus(id: Int, status: ApprovalStatus) = dbQuery {
    return@dbQuery try {
        Supplier.findById(id)
            ?.let {
                it.status = status
                it.updatedAt = LocalDateTime.now()
            }
    } catch (e: Exception) {
        throw e
    }
}

/*
*Thi function approves all the suppliers statuses in the database
 */
suspend fun updateAllSuppliersStatus(status: ApprovalStatus) = dbQuery {
    return@dbQuery try {
        Supplier.all()
            .forEach {
                it.status = status
                it.updatedAt = LocalDateTime.now()
            }
    } catch (e: Exception) {
        throw e
    }
}

suspend fun getAllSuppliers(): List<SupplierResponse> = dbQuery {
    return@dbQuery try {
        Supplier.all()
            .map { it.toSupplierResponse() }
    } catch (e: Exception) {
        throw e
    }
}

suspend fun updateSupplier(id: Int, supplierUpdateRequest: SupplierUpdate): SupplierResponse? = dbQuery {
    return@dbQuery try {
        val supplier = Supplier.findById(id)
        supplier?.apply {
            this.username = supplierUpdateRequest.username
            this.firstName = supplierUpdateRequest.firstName
            this.lastName = supplierUpdateRequest.lastName
            this.phone = supplierUpdateRequest.phone
            this.address = SupplierAddress[supplierUpdateRequest.address]
            this.email = supplierUpdateRequest.email
            this.password = hashedPassword(supplierUpdateRequest.password)
            this.updatedAt = LocalDateTime.now()
        }?.toSupplierResponse()
    } catch (e: Exception) {
        throw e
    }
}

suspend fun deleteSupplier(id: Int): Boolean = dbQuery {
    return@dbQuery try {
        Supplier.findById(id)
            ?.delete()
        true
    } catch (e: Exception) {
        throw Exception("Supplier already deleted")
    }
}