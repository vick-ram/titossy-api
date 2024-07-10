package com.example.db.supplier

import com.example.db.util.*
import com.example.models.supplier.SupplierResponse
import com.example.models.util.ApprovalStatus
import org.jetbrains.exposed.dao.id.EntityID
import java.util.*

object SupplierTable : CustomUUIDTable("suppliers") {
    val fullName = varchar("full_name", 100).uniqueIndex()
    val phone = varchar("phone", 20)
    val address = text("address")
    val email = varchar("email", 50)
    val password = varchar("password", 50)
    val status = customEnumeration(
        "status",
        "approvalstatus",
        { value -> ApprovalStatus.valueOf(value as String) },
        { PGEnum("approvalstatus", it) }
    ).default(ApprovalStatus.PENDING)
    val tsv = tsVector("tsv")
}


class Supplier(id: EntityID<UUID>) : CustomUUIDEntity(id, SupplierTable) {
    companion object : CustomUUIDEntityClass<Supplier>(SupplierTable)

    var fullName by SupplierTable.fullName
    var phone by SupplierTable.phone
    var address by SupplierTable.address
    var email by SupplierTable.email
    var password by SupplierTable.password
    var status by SupplierTable.status
    var tsv by SupplierTable.tsv

    fun toSupplierResponse() = SupplierResponse(
        id = id.value,
        fullName = fullName,
        phone = phone,
        address = address,
        email = email,
        password = password,
        status = status,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}