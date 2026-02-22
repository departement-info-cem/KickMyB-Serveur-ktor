package ca.cem.ktormyb.dto

import kotlinx.serialization.Serializable

@Serializable
data class LoginResponse(val nomUtilisateur: String)

@Serializable
data class HomeItem(
    val id: Long,
    val nom: String,
    val pourcentageAvancement: Int,
    val dateLimite: Long,
    val pourcentageTemps: Int
)

@Serializable
data class ProgressChange(val valeur: Int, val dateChangement: Long)

@Serializable
data class TaskDetail(
    val id: Long,
    val nom: String,
    val pourcentageTemps: Int,
    val pourcentageAvancement: Int,
    val dateLimite: Long,
    val changements: List<ProgressChange>
)

@Serializable
data class HomeItemWithPhoto(
    val id: Long,
    val nom: String,
    val pourcentageAvancement: Int,
    val dateLimite: Long,
    val pourcentageTemps: Int,
    val idPhoto: Long?
)

@Serializable
data class TaskDetailWithPhoto(
    val id: Long,
    val nom: String,
    val pourcentageTemps: Int,
    val pourcentageAvancement: Int,
    val dateLimite: Long,
    val changements: List<ProgressChange>,
    val idPhoto: Long?
)
