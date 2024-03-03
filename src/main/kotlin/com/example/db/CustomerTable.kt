package com.example.db

import org.jetbrains.exposed.sql.Table

object CustomerTable : Table() {
    val id = integer("id").autoIncrement()
    val username = varchar("username", 50)
    val firstName = varchar("first_name", 50)
    val lastName = varchar("last_name", 50)
    val gender = varchar("gender", 10)
    val phone = long("phone")
    val email = varchar("email", 50)
    val password = varchar("password", 50)
    val status = varchar("status", 10)

    override val primaryKey = PrimaryKey(id)
}
