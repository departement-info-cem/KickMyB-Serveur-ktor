package ca.cem.ktormyb.routes

import ca.cem.ktormyb.config.UserSession
import ca.cem.ktormyb.service.PhotoService
import ca.cem.ktormyb.service.TaskService
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.photoRoutes(photoService: PhotoService, taskService: TaskService) {
    authenticate("auth-session") {

        post("/fichier") {
            var fileBytes: ByteArray? = null
            var fileContentType = "image/jpeg"
            var taskId: Long? = null

            call.receiveMultipart().forEachPart { part ->
                when (part) {
                    is PartData.FileItem -> {
                        fileBytes = part.streamProvider().readBytes()
                        fileContentType = part.contentType?.toString() ?: "image/jpeg"
                    }
                    is PartData.FormItem -> {
                        if (part.name == "taskID") taskId = part.value.toLongOrNull()
                    }
                    else -> {}
                }
                part.dispose()
            }

            val bytes = fileBytes
                ?: return@post call.respond(HttpStatusCode.BadRequest, "No file provided")
            val id = taskId
                ?: return@post call.respond(HttpStatusCode.BadRequest, "No taskID provided")

            val photoId = photoService.storePhoto(bytes, fileContentType, id)
            call.respond(photoId.toString())
        }

        get("/fichier/{id}") {
            val photoId = call.parameters["id"]?.toLongOrNull()
                ?: return@get call.respond(HttpStatusCode.BadRequest, "Invalid photo ID")
            val maxWidth = call.request.queryParameters["largeur"]?.toIntOrNull()

            val (bytes, contentType) = photoService.getPhoto(photoId, maxWidth)
                ?: return@get call.respond(HttpStatusCode.NotFound)

            call.respondBytes(bytes, ContentType.parse(contentType))
        }

        get("/api/accueil/photo") {
            val session = call.principal<UserSession>()!!
            call.respond(taskService.getHomeItemsWithPhotos(session.userId))
        }

        get("/api/detail/photo/{id}") {
            val session = call.principal<UserSession>()!!
            val taskId = call.parameters["id"]?.toLongOrNull()
                ?: return@get call.respond(HttpStatusCode.BadRequest, "Invalid task ID")
            call.respond(taskService.getTaskDetailWithPhoto(taskId, session.userId))
        }
    }
}
