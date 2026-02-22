package ca.cem.ktormyb.service

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.Message
import com.google.firebase.messaging.Notification
import io.ktor.server.config.*
import org.slf4j.LoggerFactory
import java.io.File
import java.io.FileInputStream

class FirebaseService(config: ApplicationConfig) {

    private val logger = LoggerFactory.getLogger(FirebaseService::class.java)
    private var ready = false

    init {
        val path = config.propertyOrNull("firebase.configPath")?.getString()
            ?: "./firebase-service-account-key.json"
        val file = File(path)
        if (file.exists()) {
            try {
                val options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(FileInputStream(file)))
                    .build()
                if (FirebaseApp.getApps().isEmpty()) FirebaseApp.initializeApp(options)
                ready = true
                logger.info("Firebase initialised from $path")
            } catch (e: Exception) {
                logger.warn("Firebase initialisation failed: ${e.message}. Push notifications disabled.")
            }
        } else {
            logger.warn("Firebase config not found at '$path'. Push notifications disabled.")
        }
    }

    fun sendNotification(token: String, title: String, body: String) {
        if (!ready || token.isBlank()) return
        try {
            val message = Message.builder()
                .setNotification(Notification.builder().setTitle(title).setBody(body).build())
                .setToken(token)
                .build()
            FirebaseMessaging.getInstance().send(message)
            logger.info("Notification sent → ${token.take(12)}…")
        } catch (e: Exception) {
            logger.error("Failed to send notification: ${e.message}")
        }
    }
}
