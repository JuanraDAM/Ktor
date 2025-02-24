package com.example.data.db

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption

object ItemsTable : IntIdTable("items") {
    val title = varchar("title", 100)
    val description = varchar("description", 255).nullable()
    val weight = integer("weight")
    val image = text("image") // Texto largo para almacenar la cadena Base64
    val userId = reference("user_id", UsersTable, onDelete = ReferenceOption.CASCADE)
}
