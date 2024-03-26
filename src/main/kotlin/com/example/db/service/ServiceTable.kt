package com.example.db.service

import com.example.db.util.tsVector
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.datetime
import java.time.LocalDateTime

object ServiceTable: IntIdTable() {
    val name = varchar("name", 50).uniqueIndex()
    val category = reference("category", CategoryTable)
    val description = varchar("description", 255)
    val price = decimal("price", 10, 2)
    val duration = integer("duration")
    val imageUrl = varchar("image_url", 100).nullable()
    val createdAt = datetime("created_at").clientDefault { LocalDateTime.now() }
    val updatedAt = datetime("updated_at").clientDefault { LocalDateTime.now() }
    val tsv = tsVector("tsv")
}

object CategoryTable: IntIdTable() {
    val name = varchar("name", 50).uniqueIndex()
}

class Category(id: EntityID<Int>): IntEntity(id) {
    companion object: IntEntityClass<Category>(CategoryTable)
    var name by CategoryTable.name
}

class Service(id: EntityID<Int>): IntEntity(id) {
    companion object: IntEntityClass<Service>(ServiceTable)
    var name by ServiceTable.name
    var category by Category referencedOn ServiceTable.category
    var description by ServiceTable.description
    var price by ServiceTable.price
    var duration by ServiceTable.duration
    var imageUrl by ServiceTable.imageUrl
    var createdAt by ServiceTable.createdAt
    var updatedAt by ServiceTable.updatedAt
    var tsv by ServiceTable.tsv
}