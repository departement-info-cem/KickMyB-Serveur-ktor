package ca.cem.ktormyb.routes

import ca.cem.ktormyb.config.UserSession
import ca.cem.ktormyb.dto.AddTaskRequest
import ca.cem.ktormyb.service.TaskService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.taskRoutes(taskService: TaskService) {
    authenticate("auth-session") {

        post("/tache/ajout") {
            val session = call.principal<UserSession>()!!
            val request = call.receive<AddTaskRequest>()
            taskService.addTask(request.nom, request.dateLimite, session.userId)
            call.respond("")
        }

        get("/tache/progres/{idTache}/{valeur}") {
            val session = call.principal<UserSession>()!!
            val taskId = call.parameters["idTache"]?.toLongOrNull()
                ?: return@get call.respond(HttpStatusCode.BadRequest, "Invalid task ID")
            val value = call.parameters["valeur"]?.toIntOrNull()
                ?: return@get call.respond(HttpStatusCode.BadRequest, "Invalid value")
            taskService.updateProgress(taskId, value, session.userId)
            call.respond("")
        }

        get("/tache/accueil") {
            val session = call.principal<UserSession>()!!
            call.respond(taskService.getHomeItems(session.userId))
        }

        get("/tache/detail/{id}") {
            val session = call.principal<UserSession>()!!
            val taskId = call.parameters["id"]?.toLongOrNull()
                ?: return@get call.respond(HttpStatusCode.BadRequest, "Invalid task ID")
            call.respond(taskService.getTaskDetail(taskId, session.userId))
        }
    }
}
