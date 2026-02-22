package ca.cem.ktormyb.model

import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.ReferenceOption

object Users : LongIdTable("users") {
    val name = varchar("name", 255).uniqueIndex()
    val password = varchar("password", 255)
    val firebaseToken = varchar("firebase_token", 500).nullable()
}

object Tasks : LongIdTable("tasks") {
    val name = text("name") // AES-encrypted at the service layer
    val creationDate = long("creation_date") // epoch millis
    val deadline = long("deadline") // epoch millis
    val userId = reference("user_id", Users, onDelete = ReferenceOption.CASCADE)
}

object Advancements : LongIdTable("advancements") {
    val percentage = integer("percentage")
    val date = long("date") // epoch millis
    val taskId = reference("task_id", Tasks, onDelete = ReferenceOption.CASCADE)
}

object Photos : LongIdTable("photos") {
    val data = blob("data")
    val contentType = varchar("content_type", 100)
    val taskId = reference("task_id", Tasks, onDelete = ReferenceOption.CASCADE).uniqueIndex()
}
