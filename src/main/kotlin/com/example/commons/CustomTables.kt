package com.example.commons

import org.jetbrains.exposed.dao.*
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.javatime.datetime
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*

abstract class CustomUUIDTable(
    name: String = "",
    columnName: String = "id"
) : UUIDTable(name, columnName) {
    val createdAt = datetime("created_at")
        .clientDefault { LocalDateTime.now() }
    val updatedAt = datetime("updated_at")
        .clientDefault { LocalDateTime.now() }
}

abstract class CustomUUIDEntity(
    id: EntityID<UUID>,
    table: CustomUUIDTable
) : UUIDEntity(id) {
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

abstract class CustomStringTable(
    name: String = "",
    columnName: String = "id"
) : IdTable<String>(name) {
    private val randomId = varchar(
        columnName,
        10
    ).clientDefault { generateRandomString(10) }.uniqueIndex()
    override val id: Column<EntityID<String>> = randomId.entityId()
    val createdAt = datetime("created_at")
        .clientDefault { LocalDateTime.now() }
    val updatedAt = datetime("updated_at")
        .clientDefault { LocalDateTime.now() }
}

abstract class CustomStringEntity(
    id: EntityID<String>,
    table: CustomStringTable
) : Entity<String>(id) {
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

abstract class StringUUIDTable(
    name: String = "",
    columnName: String = "id"
) : IdTable<String>(name) {
    private val randomUUID = varchar(columnName, 10)
        .clientDefault { shortUUID() }
        .uniqueIndex()
    override val id: Column<EntityID<String>> = randomUUID.entityId()
    val createdAt = datetime("created_at")
        .clientDefault { LocalDateTime.now() }
    val updatedAt = datetime("updated_at")
        .clientDefault { LocalDateTime.now() }
}

abstract class StringUUIDEntity(
    id: EntityID<String>,
    table: StringUUIDTable
) : Entity<String>(id) {
    var createdAt by table.createdAt
    var updatedAt by table.updatedAt
}

abstract class StringUUIDEntityClass<E : StringUUIDEntity>(table: StringUUIDTable) :
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

