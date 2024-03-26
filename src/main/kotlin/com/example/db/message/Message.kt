package com.example.db.message

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.datetime
import java.time.LocalDateTime

object MessageTable: IntIdTable(){
    val message = varchar("message", 255)
    val username = varchar("username", 255).uniqueIndex()
    val timestamp = datetime("timestamp").clientDefault { LocalDateTime.now() }
}

class Message(id: EntityID<Int>): IntEntity(id){
    companion object: IntEntityClass<Message>(MessageTable)

    var message by MessageTable.message
    var username by MessageTable.username
    var timestamp by MessageTable.timestamp
}