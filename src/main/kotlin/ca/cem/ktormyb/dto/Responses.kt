package ca.cem.ktormyb.dto

import ca.cem.ktormyb.config.EpochMillisToIsoStringSerializer
import kotlinx.serialization.Serializable

@Serializable
data class LoginResponse(val nomUtilisateur: String)

@Serializable
data class HomeItem(
    val id: Long,
    val nom: String,
    val pourcentageAvancement: Int,
    @Serializable(with = EpochMillisToIsoStringSerializer::class)
    val dateLimite: Long,
    val pourcentageTemps: Int
)

@Serializable
data class ProgressChange(val valeur: Int, @Serializable(with = EpochMillisToIsoStringSerializer::class) val dateChangement: Long)

@Serializable
data class TaskDetail(
    val id: Long,
    val nom: String,
    val pourcentageTemps: Int,
    val pourcentageAvancement: Int,
    @Serializable(with = EpochMillisToIsoStringSerializer::class)
    val dateLimite: Long,
    val changements: List<ProgressChange>
)

@Serializable
data class HomeItemWithPhoto(
    val id: Long,
    val nom: String,
    val pourcentageAvancement: Int,
    @Serializable(with = EpochMillisToIsoStringSerializer::class)
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
    @Serializable(with = EpochMillisToIsoStringSerializer::class)
    val dateLimite: Long,
    val changements: List<ProgressChange>,
    val idPhoto: Long?
)
