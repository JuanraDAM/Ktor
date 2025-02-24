package com.example.domain.repositories

import com.example.domain.models.User

interface UserRepository {
    suspend fun registerUser(user: User): Int?
    suspend fun loginUser(email: String, password: String): User?
    suspend fun getUsers(): List<User>
    suspend fun updateUser(id: Int, user: User): Boolean
    suspend fun deleteUser(id: Int): Boolean
}
