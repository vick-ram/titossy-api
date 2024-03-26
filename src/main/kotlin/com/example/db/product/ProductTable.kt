package com.example.db.product

import com.example.db.util.tsVector
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable

object ProductTable: IntIdTable() {
    val name = varchar("name", 255)
    val description = text("description")
    val price = decimal("price", 10, 2)
    val image = varchar("image", 255)
    val tsv = tsVector("tsv")
}

class Product(id: EntityID<Int>) : IntEntity(id) {
    companion object: IntEntityClass<Product>(ProductTable)

    var name by ProductTable.name
    var description by ProductTable.description
    var price by ProductTable.price
    var image by ProductTable.image
    var tsv by ProductTable.tsv
}