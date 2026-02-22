package ca.cem.ktormyb.routes

import ca.cem.ktormyb.service.FirebaseService
import io.ktor.server.application.*
import io.ktor.server.html.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.html.*

/** Landing page rendered with Ktor's HTML DSL (kotlinx.html). */
fun Route.mvcRoutes() {

    get("/") {
        call.respondHtml {
            head {
                title { +"KtorMyB" }
                link(
                    rel = "stylesheet",
                    href = "https://cdn.jsdelivr.net/npm/bulma@0.9.4/css/bulma.min.css"
                )
                link(
                    rel = "stylesheet",
                    href = "https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css"
                )
            }
            body {
                section(classes = "hero is-primary") {
                    div(classes = "hero-body") {
                        p(classes = "title") { +"KtorMyB" }
                        p(classes = "subtitle") { +"Serveur Ktor pour le projet KickMyB" }
                    }
                }
                section(classes = "section") {
                    div(classes = "container") {
                        div(classes = "columns is-multiline") {
                            card(
                                icon = "fa-book",
                                title = "Documentation API",
                                linkHref = "/swagger-ui",
                                linkLabel = "Swagger UI",
                                buttonClass = "is-primary"
                            )
                            card(
                                icon = "fa-database",
                                title = "Base de données",
                                linkHref = "/h2-console",
                                linkLabel = "H2 Console",
                                buttonClass = "is-info"
                            )
                        }
                    }
                }
            }
        }
    }

    get("/index") {
        call.respondHtml {
            head { title { +"KtorMyB - Index" } }
            body {
                h1 { +"KtorMyB Server" }
                p { +"Bienvenue sur le serveur KtorMyB" }
            }
        }
    }
}

fun Route.notificationTestRoutes(firebaseService: FirebaseService) {
    post("/test/notifications") {
        val token = call.receiveText()
        firebaseService.sendNotification(token, "Test KtorMyB", "Test notification envoyée depuis KtorMyB")
        call.respond("Notification envoyée")
    }
}

// ── DSL helper ────────────────────────────────────────────────────────────────

private fun FlowContent.card(
    icon: String,
    title: String,
    linkHref: String,
    linkLabel: String,
    buttonClass: String
) {
    div(classes = "column is-one-third") {
        div(classes = "card") {
            div(classes = "card-content") {
                p(classes = "title is-5") {
                    span(classes = "icon") { i(classes = "fas $icon") {} }
                    +" $title"
                }
                a(href = linkHref, classes = "button $buttonClass") { +linkLabel }
            }
        }
    }
}
