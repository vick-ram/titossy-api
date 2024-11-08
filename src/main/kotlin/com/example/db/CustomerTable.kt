package com.example.db

import com.example.commons.*
import com.example.models.CustomerResponse
import org.jetbrains.exposed.dao.id.EntityID

object CustomerTable : StringUUIDTable("customers") {
    val fullName = varchar("full_name", 100).uniqueIndex()
    val phone = varchar("phone", 20)
    val address = text("address", eagerLoading = true).nullable()
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

class Customer(id: EntityID<String>) : StringUUIDEntity(id, CustomerTable) {
    companion object : StringUUIDEntityClass<Customer>(CustomerTable)

    var fullName by CustomerTable.fullName
    var phone by CustomerTable.phone
    var address by CustomerTable.address
    var email by CustomerTable.email
    var password by CustomerTable.password
    var status by CustomerTable.status
    var tsv by CustomerTable.tsv

    fun toCustomerResponse() = CustomerResponse(
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
