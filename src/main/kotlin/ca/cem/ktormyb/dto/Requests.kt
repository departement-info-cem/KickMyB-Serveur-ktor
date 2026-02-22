package ca.cem.ktormyb.dto

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(val nom: String, val motDePasse: String)

@Serializable
data class RegisterRequest(
    val nom: String,
    val motDePasse: String,
    val confirmationMotDePasse: String
)

@Serializable
data class AddTaskRequest(
    val nom: String,
    val dateLimite: Long // epoch millis
)
