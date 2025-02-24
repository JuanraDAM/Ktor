package com.example.domain.usecases

import com.example.domain.models.User
import com.example.domain.repositories.UserRepository

class RegisterUserUseCase(private val repository: UserRepository) {
    /**
     * Registra un usuario.
     * Se espera que en el repositorio se cifre la contraseña antes de almacenar.
     */
    suspend operator fun invoke(user: User): Int? {
        return repository.registerUser(user)
    }
}
