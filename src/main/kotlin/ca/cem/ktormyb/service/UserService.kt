package ca.cem.ktormyb.service

import at.favre.lib.crypto.bcrypt.BCrypt
import ca.cem.ktormyb.config.UserSession
import ca.cem.ktormyb.config.dbQuery
import ca.cem.ktormyb.dto.LoginRequest
import ca.cem.ktormyb.dto.RegisterRequest
import ca.cem.ktormyb.exception.*
import ca.cem.ktormyb.model.Users
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

class UserService {

    suspend fun login(request: LoginRequest): UserSession = dbQuery {
        val row = Users.selectAll()
            .where { Users.name eq request.nom.trim().lowercase() }
            .singleOrNull()
            ?: throw MauvaisNomOuMotDePasse()

        val verified = BCrypt.verifyer()
            .verify(request.motDePasse.toCharArray(), row[Users.password])
            .verified
        if (!verified) throw MauvaisNomOuMotDePasse()

        UserSession(userId = row[Users.id].value, username = row[Users.name])
    }

    suspend fun register(request: RegisterRequest) {
        val nom = request.nom.trim().lowercase()
        if (nom.length < 2) throw NomTropCourt()
        if (nom.length > 255) throw NomTropLong()
        if (request.motDePasse.length < 4) throw MotDePasseTropCourt()
        if (request.motDePasse.length > 255) throw MotDePasseTropLong()
        if (request.motDePasse != request.confirmationMotDePasse) throw MotsDePasseDifferents()

        dbQuery {
            if (Users.selectAll().where { Users.name eq nom }.count() > 0) throw NomDejaPris()

            val hash = BCrypt.withDefaults().hashToString(12, request.motDePasse.toCharArray())
            Users.insert {
                it[name] = nom
                it[password] = hash
            }
        }
    }

    suspend fun registerFirebaseToken(userId: Long, token: String) = dbQuery {
        Users.update({ Users.id eq userId }) {
            it[firebaseToken] = token.trim()
        }
    }
}
