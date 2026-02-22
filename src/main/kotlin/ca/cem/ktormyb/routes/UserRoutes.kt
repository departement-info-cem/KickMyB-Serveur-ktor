package ca.cem.ktormyb.routes

import ca.cem.ktormyb.config.UserSession
import ca.cem.ktormyb.dto.LoginRequest
import ca.cem.ktormyb.dto.LoginResponse
import ca.cem.ktormyb.dto.RegisterRequest
import ca.cem.ktormyb.service.UserService
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*

fun Route.userRoutes(userService: UserService) {

    post("/id/connexion") {
        val request = call.receive<LoginRequest>()
        val session = userService.login(request)
        call.sessions.set(session)
        call.respond(LoginResponse(nomUtilisateur = session.username))
    }

    post("/id/inscription") {
        val request = call.receive<RegisterRequest>()
        userService.register(request)
        // Auto-login after successful registration
        val session = userService.login(LoginRequest(nom = request.nom, motDePasse = request.motDePasse))
        call.sessions.set(session)
        call.respond(LoginResponse(nomUtilisateur = session.username))
    }

    authenticate("auth-session") {

        post("/id/deconnexion") {
            call.sessions.clear<UserSession>()
            call.respond("")
        }

        post("/enregistrer-jeton-notification") {
            val session = call.principal<UserSession>()!!
            val token = call.receiveText()
            userService.registerFirebaseToken(session.userId, token)
            call.respond("TokenEnregistre")
        }
    }
}
