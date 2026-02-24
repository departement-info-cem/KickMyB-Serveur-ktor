package ca.cem.ktormyb.config

import ca.cem.ktormyb.model.Advancements
import ca.cem.ktormyb.model.Photos
import ca.cem.ktormyb.model.Tasks
import ca.cem.ktormyb.model.Users
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.application.*
import kotlinx.coroutines.Dispatchers
import org.h2.tools.Server
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction

fun Application.initDatabase() {
    val dbUrl = environment.config.propertyOrNull("database.url")?.getString()
        ?: "jdbc:h2:file:./db/ktormyb"
    val dbDriver = environment.config.propertyOrNull("database.driver")?.getString()
        ?: "org.h2.Driver"
    val dbUser = environment.config.propertyOrNull("database.user")?.getString() ?: "sa"
    val dbPassword = environment.config.propertyOrNull("database.password")?.getString() ?: ""

    val hikariConfig = HikariConfig().apply {
        jdbcUrl = dbUrl
        driverClassName = dbDriver
        username = dbUser
        password = dbPassword
        maximumPoolSize = 10
    }

    Database.connect(HikariDataSource(hikariConfig))

    // Enable H2 Console
    Server.createWebServer("-webPort", "8081").start()

    transaction {
        SchemaUtils.createMissingTablesAndColumns(Users, Tasks, Advancements, Photos)
    }
}

/** Runs [block] inside a coroutine-safe Exposed transaction on the IO dispatcher. */
suspend fun <T> dbQuery(block: suspend () -> T): T =
    newSuspendedTransaction(Dispatchers.IO) { block() }
