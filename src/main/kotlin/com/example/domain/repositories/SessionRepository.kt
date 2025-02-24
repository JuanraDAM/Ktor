package com.example.domain.repositories

import com.example.domain.models.Session

interface SessionRepository {
    suspend fun createSession(userId: Int, token: String): Session
    suspend fun getSessionByToken(token: String): Session?
    suspend fun deleteSession(token: String): Boolean
}