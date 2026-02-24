package ca.cem.ktormyb.dto

import ca.cem.ktormyb.config.EpochMillisToIsoStringSerializer
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
    @Serializable(with = EpochMillisToIsoStringSerializer::class)
    val dateLimite: Long // internally stored as epoch millis
)


