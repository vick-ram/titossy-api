package com.example.db.inventory

import com.example.db.employee.Employee
import com.example.db.employee.EmployeeTable
import com.example.db.product.Product
import com.example.db.product.ProductTable
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.javatime.datetime

object InventoryTable: IntIdTable() {
    val productId = reference("product_id", ProductTable, onDelete = ReferenceOption.CASCADE)
    val quantity = integer("quantity")
    val lastUpdate = datetime("last_update")
    val employeeId = reference("employee_id", EmployeeTable, onDelete = ReferenceOption.CASCADE)
}

class Inventory(id: EntityID<Int>): IntEntity(id) {
    companion object: IntEntityClass<Inventory>(InventoryTable)

    var product by Product referencedOn InventoryTable.productId
    var quantity by InventoryTable.quantity
    var lastUpdate by InventoryTable.lastUpdate
    var employee by Employee referencedOn InventoryTable.employeeId
}