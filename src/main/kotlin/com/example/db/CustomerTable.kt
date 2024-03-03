package com.example.db

import org.jetbrains.exposed.dao.id.IntIdTable

object CustomerTable : IntIdTable() {
    val username = varchar("username", 50)
    val firstName = varchar("first_name", 50)
    val lastName = varchar("last_name", 50)
    val gender = varchar("gender", 10)
    val phone = long("phone")
    val address = reference("address", AddressTable.id)
    val email = varchar("email", 50)
    val password = varchar("password", 50)
    val status = varchar("status", 10)
}

object AddressTable : IntIdTable() {
    val street = varchar("street", 50)
    val city = varchar("city", 50)
    val state = varchar("state", 50)
    val zip = varchar("zip", 10)
}
