package com.example.db.customer

import com.example.db.util.PGEnum
import com.example.models.customer.Gender
import com.example.models.util.ApprovalStatus
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.javatime.datetime
import java.time.LocalDateTime

object CustomerTable : IntIdTable() {
    val username = varchar("username", 50).uniqueIndex()
    val firstName = varchar("first_name", 50)
    val lastName = varchar("last_name", 50)
    val gender = customEnumeration(
        "gender",
        "gender",
        { value -> Gender.valueOf(value as String) },
        { PGEnum("gender", it) }
    )
    val phone = long("phone")
    val address =
        reference("address", CustomerAddressTable, onDelete = ReferenceOption.CASCADE)
    val email = varchar("email", 50)
    val password = varchar("password", 50)
    val status = customEnumeration(
        "status",
        "approvalstatus",
        { value -> ApprovalStatus.valueOf(value as String) },
        { PGEnum("approvalstatus", it)})
    val createdAt = datetime("created_at").clientDefault { LocalDateTime.now() }
    val updatedAt = datetime("updated_at").clientDefault { LocalDateTime.now() }
}

object CustomerAddressTable : IntIdTable() {
    val street = varchar("street", 50)
    val city = varchar("city", 50)
    val state = varchar("state", 50)
    val zip = varchar("zip", 10)
}

class CustomerAddress(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<CustomerAddress>(CustomerAddressTable)

    var street by CustomerAddressTable.street
    var city by CustomerAddressTable.city
    var state by CustomerAddressTable.state
    var zip by CustomerAddressTable.zip
}

class Customer(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<Customer>(CustomerTable)

    var username by CustomerTable.username
    var firstName by CustomerTable.firstName
    var lastName by CustomerTable.lastName
    var gender by CustomerTable.gender
    var phone by CustomerTable.phone
    var address by CustomerAddress referencedOn CustomerTable.address
    var email by CustomerTable.email
    var password by CustomerTable.password
    var status by CustomerTable.status
    var createdAt by CustomerTable.createdAt
    var updatedAt by CustomerTable.updatedAt
}
