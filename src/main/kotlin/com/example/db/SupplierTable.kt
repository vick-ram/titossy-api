package com.example.db

import com.example.commons.*
import com.example.models.SupplierResponse
import org.jetbrains.exposed.dao.id.EntityID

object SupplierTable : StringUUIDTable("suppliers") {
    val fullName = varchar("full_name", 100).uniqueIndex()
    val company = varchar("company", 100)
    val phone = varchar("phone", 20)
    val address = text("address")
    val email = varchar("email", 50)
    val password = varchar("password", 250)
    val status = customEnumeration(
        "status",
        "approvalstatus",
        { value -> ApprovalStatus.valueOf(value as String) },
        { PGEnum("approvalstatus", it) }
    ).default(ApprovalStatus.PENDING)
    val tsv = tsVector("tsv")
}


class Supplier(id: EntityID<String>) : StringUUIDEntity(id, SupplierTable) {
    companion object : StringUUIDEntityClass<Supplier>(SupplierTable)

    var fullName by SupplierTable.fullName
    var company by SupplierTable.company
    var phone by SupplierTable.phone
    var address by SupplierTable.address
    var email by SupplierTable.email
    var password by SupplierTable.password
    var status by SupplierTable.status
    var tsv by SupplierTable.tsv

    fun toSupplierResponse() = SupplierResponse(
        id = id.value,
        fullName = fullName,
        company = company,
        phone = phone,
        address = address,
        email = email,
        password = password,
        status = status,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}