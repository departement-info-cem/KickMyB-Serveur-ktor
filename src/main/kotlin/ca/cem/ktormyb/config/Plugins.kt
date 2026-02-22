package ca.cem.ktormyb.config

import ca.cem.ktormyb.exception.ServiceException
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.sessions.*
import kotlinx.serialization.json.Json

/** Holds the authenticated user's identity; stored in a signed cookie. */
data class UserSession(val userId: Long, val username: String) : Principal

fun Application.configureSessions() {
    val secret = environment.config.propertyOrNull("session.secret")?.getString()
        ?: "change-me-in-production-32bytes!!"

    install(Sessions) {
        cookie<UserSession>("USER_SESSION") {
            cookie.path = "/"
            cookie.httpOnly = true
            cookie.maxAgeInSeconds = 86_400 * 7 // 1 week
            // Sign the cookie to prevent tampering
            transform(SessionTransportTransformerMessageAuthentication(secret.toByteArray()))
        }
    }

    install(Authentication) {
        session<UserSession>("auth-session") {
            validate { session -> session }
            challenge {
                call.respond(HttpStatusCode.Unauthorized, "Unauthorized")
            }
        }
    }
}

fun Application.configureContentNegotiation() {
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
        })
    }
}

fun Application.configureStatusPages() {
    install(StatusPages) {
        // All domain exceptions → 400 with the exception class name as body
        exception<ServiceException> { call, cause ->
            call.respond(HttpStatusCode.BadRequest, cause.message ?: "Error")
        }
        exception<IllegalArgumentException> { call, cause ->
            call.respond(HttpStatusCode.BadRequest, cause.message ?: "Bad request")
        }
        exception<Throwable> { call, cause ->
            this@configureStatusPages.log.error("Unhandled exception", cause)
            call.respond(HttpStatusCode.InternalServerError, "Internal server error")
        }
    }
}
