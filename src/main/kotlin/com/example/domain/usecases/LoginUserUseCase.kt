package com.example.domain.usecases

import com.example.domain.models.User
import com.example.domain.repositories.UserRepository

class LoginUserUseCase(private val repository: UserRepository) {
    /**
     * Realiza la autenticación de un usuario usando su correo y contraseña.
     */
    suspend operator fun invoke(email: String, password: String): User? {
        return repository.loginUser(email, password)
    }
}
