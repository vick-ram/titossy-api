package com.example.db.employee

import com.example.db.util.PGEnum
import com.example.models.employee.Availability
import com.example.models.employee.Roles
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.datetime
import java.time.LocalDateTime

object EmployeeTable : IntIdTable() {
    val username = varchar("username", 50)
    val firstName = varchar("first_name", 50)
    val lastName = varchar("last_name", 50)
    val gender = varchar("gender", 50)
    val email = varchar("email", 50)
    val password = varchar("password", 50)
    val phone = long("phone")
    val role = customEnumeration(
        "role",
        "roles",
        { value -> Roles.valueOf(value as String) },
        { PGEnum("roles", it) })
    val availability = customEnumeration(
        "availability",
        "availability",
        { value -> Availability.valueOf(value as String) },
        { PGEnum("availability", it) }
    ).nullable()
    val createdAt = datetime("created_at").clientDefault { LocalDateTime.now() }
    val updatedAt = datetime("updated_at").clientDefault { LocalDateTime.now() }
}

class Employee(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<Employee>(EmployeeTable)

    var username by EmployeeTable.username
    var firstName by EmployeeTable.firstName
    var lastName by EmployeeTable.lastName
    var gender by EmployeeTable.gender
    var email by EmployeeTable.email
    var password by EmployeeTable.password
    var phone by EmployeeTable.phone
    var role by EmployeeTable.role
    var availability by EmployeeTable.availability
    var createdAt by EmployeeTable.createdAt
    var updatedAt by EmployeeTable.updatedAt
}