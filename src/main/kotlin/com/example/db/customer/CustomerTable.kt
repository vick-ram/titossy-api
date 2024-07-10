package com.example.db.customer

import com.example.db.util.*
import com.example.models.customer.CustomerResponse
import com.example.models.util.ApprovalStatus
import org.jetbrains.exposed.dao.id.EntityID
import java.util.*

object CustomerTable : CustomUUIDTable("customers") {
    val username = varchar("username", 50).uniqueIndex()
    val fullName = varchar("full_name", 100)
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

class Customer(id: EntityID<UUID>) : CustomUUIDEntity(id, CustomerTable) {
    companion object : CustomUUIDEntityClass<Customer>(CustomerTable)

    var username by CustomerTable.username
    var fullName by CustomerTable.fullName
    var phone by CustomerTable.phone
    var address by CustomerTable.address
    var email by CustomerTable.email
    var password by CustomerTable.password
    var status by CustomerTable.status
    var tsv by CustomerTable.tsv

    fun toCustomerResponse() = CustomerResponse(
        id = id.value,
        username = username,
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
