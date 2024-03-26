package com.example.db.supplier

import com.example.db.util.PGEnum
import com.example.models.util.ApprovalStatus
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.javatime.datetime
import java.time.LocalDateTime

object SupplierTable : IntIdTable() {
    val username = varchar("username", 50).uniqueIndex()
    val firstName = varchar("first_name", 50)
    val lastName = varchar("last_name", 50)
    val phone = long("phone")
    val address = reference("address", SupplierAddressTable, onDelete = ReferenceOption.CASCADE)
    val email = varchar("email", 50)
    val password = varchar("password", 50)
    val status = customEnumeration(
        "status",
        "approvalstatus",
        { value -> ApprovalStatus.valueOf(value as String) },
        { PGEnum("approvalstatus", it) })
    val createdAt = datetime("created_at").clientDefault { LocalDateTime.now() }
    val updatedAt = datetime("updated_at").clientDefault { LocalDateTime.now() }
}

object SupplierAddressTable : IntIdTable() {
    val street = varchar("street", 50)
    val city = varchar("city", 50)
    val state = varchar("state", 50)
    val zip = varchar("zip", 10)
}

class SupplierAddress(id: EntityID<Int>): IntEntity(id) {
    companion object: IntEntityClass<SupplierAddress>(SupplierAddressTable)
    var street by SupplierAddressTable.street
    var city by SupplierAddressTable.city
    var state by SupplierAddressTable.state
    var zip by SupplierAddressTable.zip
}

class Supplier(id: EntityID<Int>): IntEntity(id) {
    companion object : IntEntityClass<Supplier>(SupplierTable)

    var username by SupplierTable.username
    var firstName by SupplierTable.firstName
    var lastName by SupplierTable.lastName
    var phone by SupplierTable.phone
    var address by SupplierAddress referencedOn SupplierTable.address
    var email by SupplierTable.email
    var password by SupplierTable.password
    var status by SupplierTable.status
    var createdAt by SupplierTable.createdAt
    var updatedAt by SupplierTable.updatedAt
}