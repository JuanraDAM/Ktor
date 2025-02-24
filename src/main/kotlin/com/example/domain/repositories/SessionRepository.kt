package com.example.domain.repositories

import com.example.domain.models.Session

interface SessionRepository {
    suspend fun createSession(userId: Int, token: String): Session
    suspend fun getSessionByToken(token: String): Session?
    suspend fun getSessionById(sessionId: Int): Session?
    suspend fun updateSessionToken(sessionId: Int, token: String)
    suspend fun deleteSession(token: String): Boolean
    suspend fun deleteSessionsByUserId(userId: Int): Int
}
