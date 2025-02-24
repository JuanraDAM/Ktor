package com.example.data.db

import org.jetbrains.exposed.dao.id.IntIdTable

object UsersTable : IntIdTable("users") {
    val email = varchar("email", 100).uniqueIndex() // correo único
    val password = varchar("password", 60)          // hash de BCrypt
}
