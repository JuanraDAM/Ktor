package com.example.data.repositories

import com.example.domain.models.User
import com.example.domain.repositories.UserRepository
import com.example.data.db.UsersTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import org.mindrot.jbcrypt.BCrypt

class UserRepositoryImpl : UserRepository {

    override suspend fun registerUser(user: User): Int? = transaction {
        val exists = UsersTable.select { UsersTable.email eq user.email }.firstOrNull()
        if (exists != null) {
            null
        } else {
            val hashedPassword = BCrypt.hashpw(user.password, BCrypt.gensalt())
            UsersTable.insertAndGetId { row ->
                row[email] = user.email
                row[password] = hashedPassword
            }.value
        }
    }

    override suspend fun loginUser(email: String, password: String): User? = transaction {
        val row = UsersTable.select { UsersTable.email eq email }.firstOrNull() ?: return@transaction null
        val storedHash = row[UsersTable.password]
        return@transaction if (BCrypt.checkpw(password, storedHash)) {
            User(
                id = row[UsersTable.id].value,
                email = row[UsersTable.email],
                password = row[UsersTable.password]
            )
        } else null
    }

    override suspend fun getUsers(): List<User> = transaction {
        UsersTable.selectAll().map {
            User(
                id = it[UsersTable.id].value,
                email = it[UsersTable.email],
                password = it[UsersTable.password]
            )
        }
    }

    override suspend fun updateUser(id: Int, user: User): Boolean = transaction {
        val updated = UsersTable.update({ UsersTable.id eq id }) { row ->
            row[email] = user.email
            if (user.password.isNotEmpty()) {
                row[password] = BCrypt.hashpw(user.password, BCrypt.gensalt())
            }
        }
        updated > 0
    }

    override suspend fun deleteUser(id: Int): Boolean = transaction {
        UsersTable.deleteWhere { UsersTable.id eq id } > 0
    }

    override suspend fun getUserByEmail(email: String): User? = transaction {
        val row = UsersTable.select { UsersTable.email eq email }.firstOrNull() ?: return@transaction null
        User(
            id = row[UsersTable.id].value,
            email = row[UsersTable.email],
            password = row[UsersTable.password]
        )
    }
}
