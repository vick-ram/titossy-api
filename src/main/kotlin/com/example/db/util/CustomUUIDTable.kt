package com.example.db.util

import com.example.models.util.generateRandomString
import org.jetbrains.exposed.dao.*
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.javatime.datetime
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*

abstract class CustomUUIDTable(name: String = "", columnName: String = "id") : UUIDTable(name, columnName) {
    val createdAt = datetime("created_at").clientDefault { LocalDateTime.now() }
    val updatedAt = datetime("updated_at").clientDefault { LocalDateTime.now() }
}

abstract class CustomUUIDEntity(id: EntityID<UUID>, table: CustomUUIDTable) : UUIDEntity(id) {
    var createdAt by table.createdAt
    var updatedAt by table.updatedAt
}

abstract class CustomUUIDEntityClass<E : CustomUUIDEntity>(table: CustomUUIDTable) : UUIDEntityClass<E>(table) {
    init {
        EntityHook.subscribe { action ->
            if (action.changeType == EntityChangeType.Updated) {
                try {
                    action.toEntity(this)?.apply {
                        updatedAt = currentUtc()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
}

fun currentUtc(): LocalDateTime = LocalDateTime.now(ZoneOffset.UTC)

abstract class CustomStringTable(name: String = "", columnName: String = "id") : IdTable<String>(name) {
    private val randomId = varchar(columnName, 10).clientDefault { generateRandomString(10) }.uniqueIndex()
    override val id: Column<EntityID<String>> = randomId.entityId()
    val createdAt = datetime("created_at").clientDefault { LocalDateTime.now() }
    val updatedAt = datetime("updated_at").clientDefault { LocalDateTime.now() }
}

abstract class CustomStringEntity(id: EntityID<String>, table: CustomStringTable) : Entity<String>(id) {
    var createdAt by table.createdAt
    var updatedAt by table.updatedAt
}

abstract class CustomStringEntityClass<E : CustomStringEntity>(table: CustomStringTable) :
    EntityClass<String, E>(table) {
    init {
        EntityHook.subscribe { action ->
            if (action.changeType == EntityChangeType.Updated) {
                try {
                    action.toEntity(this)?.apply {
                        updatedAt = currentUtc()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
}