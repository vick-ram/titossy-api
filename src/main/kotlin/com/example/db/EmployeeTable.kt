package com.example.db

import com.example.commons.*
import com.example.models.EmployeeResponse
import org.jetbrains.exposed.dao.id.EntityID

object EmployeeTable : StringUUIDTable("employees") {
    val username = varchar("username", 50)
    val fullName = varchar("full_name", 100)
    val gender = enumerationByName("gender", 20, Gender::class)
    val email = varchar("email", 50)
    val password = varchar("password", 50)
    val phone = varchar("phone", 20)
    val role = customEnumeration(
        "role",
        "roles",
        { value -> Roles.valueOf(value as String) },
        { PGEnum("roles", it) }
    )
    val availability = customEnumeration(
        "availability",
        "availability",
        { value -> Availability.valueOf(value as String) },
        { PGEnum("availability", it) }
    ).nullable()
    val approvalStatus = customEnumeration(
        "status",
        "approvalstatus",
        { value -> ApprovalStatus.valueOf(value as String) },
        { PGEnum("approvalstatus", it) }
    ).default(ApprovalStatus.APPROVED).nullable()
    val tsv = tsVector("tsv")
}

class Employee(id: EntityID<String>) : StringUUIDEntity(id, EmployeeTable) {
    companion object : StringUUIDEntityClass<Employee>(EmployeeTable)

    var username by EmployeeTable.username
    var fullName by EmployeeTable.fullName
    var gender by EmployeeTable.gender
    var email by EmployeeTable.email
    var password by EmployeeTable.password
    var phone by EmployeeTable.phone
    var role by EmployeeTable.role
    var availability by EmployeeTable.availability
    var approvalStatus by EmployeeTable.approvalStatus
    var tsv by EmployeeTable.tsv

    fun toEmployeeResponse() = EmployeeResponse(
        id = id.value,
        username = username,
        fullName = fullName,
        gender = gender,
        email = email,
        password = password,
        phone = phone,
        role = role,
        availability = availability,
        approvalStatus = approvalStatus,
        createdAt = createdAt,
        updatedAt = createdAt,
    )
}