package com.example.data.repositories

import com.example.domain.models.Session
import com.example.domain.repositories.SessionRepository
import com.example.data.db.SessionsTable
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.update
import org.jetbrains.exposed.sql.transactions.transaction

class SessionRepositoryImpl : SessionRepository {
    override suspend fun createSession(userId: Int, token: String): Session = transaction {
        val id = SessionsTable.insertAndGetId { row ->
            row[SessionsTable.userId] = userId
            row[SessionsTable.token] = token
        }.value
        Session(id, userId, token)
    }

    override suspend fun getSessionByToken(token: String): Session? = transaction {
        SessionsTable.select { SessionsTable.token eq token }
            .firstOrNull()
            ?.let {
                Session(
                    id = it[SessionsTable.id].value,
                    userId = it[SessionsTable.userId].value,
                    token = it[SessionsTable.token]
                )
            }
    }

    override suspend fun getSessionById(sessionId: Int): Session? = transaction {
        SessionsTable.select { SessionsTable.id eq sessionId }
            .firstOrNull()
            ?.let {
                Session(
                    id = it[SessionsTable.id].value,
                    userId = it[SessionsTable.userId].value,
                    token = it[SessionsTable.token]
                )
            }
    }

    override suspend fun updateSessionToken(sessionId: Int, token: String) {
        transaction {
            SessionsTable.update({ SessionsTable.id eq sessionId }) {
                it[SessionsTable.token] = token
            }
        }
    }

    override suspend fun deleteSession(token: String): Boolean = transaction {
        SessionsTable.deleteWhere { SessionsTable.token eq token } > 0
    }

    override suspend fun deleteSessionsByUserId(userId: Int): Int = transaction {
        SessionsTable.deleteWhere { SessionsTable.userId eq userId }
    }
}
