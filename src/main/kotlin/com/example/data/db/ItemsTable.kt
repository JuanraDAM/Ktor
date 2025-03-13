package com.example.data.db

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption

// Definición de un tipo de columna que se mapea a LONGTEXT en la base de datos.
class LongTextColumnType : org.jetbrains.exposed.sql.ColumnType() {
    override fun sqlType(): String = "LONGTEXT"
}

object ItemsTable : IntIdTable("items") {
    val title = varchar("title", 100)
    val description = varchar("description", 255).nullable()
    val weight = integer("weight")
    // La columna "image" se guarda como LONGTEXT para cadenas largas (Base64)
    val image = registerColumn<String>("image", LongTextColumnType())
    val latitude = double("latitude").nullable()
    val longitude = double("longitude").nullable()
    val userId = reference("user_id", UsersTable, onDelete = ReferenceOption.CASCADE)
}
