package ca.cem.ktormyb

import ca.cem.ktormyb.config.*
import ca.cem.ktormyb.routes.*
import ca.cem.ktormyb.service.*
import io.ktor.server.application.*
import io.ktor.server.netty.EngineMain
import io.ktor.server.plugins.callloging.CallLogging
import io.ktor.server.routing.*

fun main(args: Array<String>) = EngineMain.main(args)

fun Application.module() {
    install(CallLogging)

    initDatabase()
    configureSessions()
    configureContentNegotiation()
    configureStatusPages()

    val userService = UserService()
    val taskService = TaskService()
    val photoService = PhotoService()
    val firebaseService = FirebaseService(environment.config)
    val scheduler = NotificationScheduler(taskService, firebaseService)
    scheduler.start()

    routing {
        mvcRoutes()
        userRoutes(userService)
        taskRoutes(taskService)
        photoRoutes(photoService, taskService)
        notificationTestRoutes(firebaseService)
    }
}
