package ca.cem.ktormyb.service

import ca.cem.ktormyb.config.dbQuery
import ca.cem.ktormyb.dto.*
import ca.cem.ktormyb.exception.*
import ca.cem.ktormyb.model.Advancements
import ca.cem.ktormyb.model.Photos
import ca.cem.ktormyb.model.Tasks
import ca.cem.ktormyb.model.Users
import ca.cem.ktormyb.security.Encryptor
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

class TaskService {

    suspend fun addTask(nom: String, deadlineMillis: Long, userId: Long) {
        val trimmed = nom.trim()
        if (trimmed.isEmpty()) throw Vide()
        if (trimmed.length < 2) throw TropCourt()
        if (trimmed.length > 255) throw TropLong()

        dbQuery {
            // Uniqueness check within the user's tasks
            val duplicate = Tasks.selectAll()
                .where { Tasks.userId eq userId }
                .any { Encryptor.decrypt(it[Tasks.name]) == trimmed }
            if (duplicate) throw Existant()

            Tasks.insert {
                it[name] = Encryptor.encrypt(trimmed)
                it[creationDate] = System.currentTimeMillis()
                it[deadline] = deadlineMillis
                it[Tasks.userId] = userId
            }
        }
    }

    suspend fun updateProgress(taskId: Long, percentage: Int, userId: Long) {
        require(percentage in 0..100) { "Percentage must be between 0 and 100" }
        dbQuery {
            Tasks.selectAll()
                .where { (Tasks.id eq taskId) and (Tasks.userId eq userId) }
                .singleOrNull()
                ?: throw IllegalArgumentException("Task not found")

            Advancements.insert {
                it[Advancements.percentage] = percentage
                it[date] = System.currentTimeMillis()
                it[Advancements.taskId] = taskId
            }
        }
    }

    suspend fun getHomeItems(userId: Long): List<HomeItem> = dbQuery {
        Tasks.selectAll().where { Tasks.userId eq userId }.map { row ->
            val taskId = row[Tasks.id].value
            val advancements = advancementsFor(taskId)
            HomeItem(
                id = taskId,
                nom = Encryptor.decrypt(row[Tasks.name]),
                pourcentageAvancement = advancements.lastOrNull() ?: 0,
                dateLimite = row[Tasks.deadline],
                pourcentageTemps = timeProgress(row[Tasks.creationDate], row[Tasks.deadline])
            )
        }
    }

    suspend fun getTaskDetail(taskId: Long, userId: Long): TaskDetail = dbQuery {
        val row = Tasks.selectAll()
            .where { (Tasks.id eq taskId) and (Tasks.userId eq userId) }
            .singleOrNull() ?: throw IllegalArgumentException("Task not found")

        val changes = advancementChangesFor(taskId)
        TaskDetail(
            id = taskId,
            nom = Encryptor.decrypt(row[Tasks.name]),
            pourcentageTemps = timeProgress(row[Tasks.creationDate], row[Tasks.deadline]),
            pourcentageAvancement = changes.lastOrNull()?.valeur ?: 0,
            dateLimite = row[Tasks.deadline],
            changements = changes
        )
    }

    suspend fun getHomeItemsWithPhotos(userId: Long): List<HomeItemWithPhoto> = dbQuery {
        Tasks.selectAll().where { Tasks.userId eq userId }.map { row ->
            val taskId = row[Tasks.id].value
            val advancements = advancementsFor(taskId)
            HomeItemWithPhoto(
                id = taskId,
                nom = Encryptor.decrypt(row[Tasks.name]),
                pourcentageAvancement = advancements.lastOrNull() ?: 0,
                dateLimite = row[Tasks.deadline],
                pourcentageTemps = timeProgress(row[Tasks.creationDate], row[Tasks.deadline]),
                idPhoto = Photos.selectAll().where { Photos.taskId eq taskId }
                    .singleOrNull()?.get(Photos.id)?.value
            )
        }
    }

    suspend fun getTaskDetailWithPhoto(taskId: Long, userId: Long): TaskDetailWithPhoto = dbQuery {
        val row = Tasks.selectAll()
            .where { (Tasks.id eq taskId) and (Tasks.userId eq userId) }
            .singleOrNull() ?: throw IllegalArgumentException("Task not found")

        val changes = advancementChangesFor(taskId)
        TaskDetailWithPhoto(
            id = taskId,
            nom = Encryptor.decrypt(row[Tasks.name]),
            pourcentageTemps = timeProgress(row[Tasks.creationDate], row[Tasks.deadline]),
            pourcentageAvancement = changes.lastOrNull()?.valeur ?: 0,
            dateLimite = row[Tasks.deadline],
            changements = changes,
            idPhoto = Photos.selectAll().where { Photos.taskId eq taskId }
                .singleOrNull()?.get(Photos.id)?.value
        )
    }

    /** Returns all (taskId, deadline, firebaseToken) triples — used by the notification scheduler. */
    suspend fun allTasksForScheduler(): List<Triple<Long, Long, String?>> = dbQuery {
        Tasks.join(Users, JoinType.INNER, onColumn = Tasks.userId, otherColumn = Users.id)
            .selectAll()
            .map { Triple(it[Tasks.id].value, it[Tasks.deadline], it[Users.firebaseToken]) }
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private fun advancementsFor(taskId: Long): List<Int> =
        Advancements.selectAll()
            .where { Advancements.taskId eq taskId }
            .orderBy(Advancements.date)
            .map { it[Advancements.percentage] }

    private fun advancementChangesFor(taskId: Long): List<ProgressChange> =
        Advancements.selectAll()
            .where { Advancements.taskId eq taskId }
            .orderBy(Advancements.date)
            .map { ProgressChange(it[Advancements.percentage], it[Advancements.date]) }

    private fun timeProgress(creationDate: Long, deadline: Long): Int {
        val now = System.currentTimeMillis()
        if (now >= deadline) return 100
        if (now <= creationDate) return 0
        val elapsed = (now - creationDate).toDouble()
        val total = (deadline - creationDate).toDouble()
        return ((elapsed / total) * 100).toInt().coerceIn(0, 100)
    }
}
