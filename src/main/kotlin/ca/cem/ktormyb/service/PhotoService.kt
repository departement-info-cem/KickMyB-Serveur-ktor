package ca.cem.ktormyb.service

import ca.cem.ktormyb.config.dbQuery
import ca.cem.ktormyb.model.Photos
import org.imgscalr.Scalr
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.statements.api.ExposedBlob
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO

class PhotoService {

    suspend fun storePhoto(bytes: ByteArray, contentType: String, taskId: Long): Long = dbQuery {
        // Replace any existing photo for this task
        Photos.deleteWhere { Photos.taskId eq taskId }

        Photos.insertAndGetId {
            it[Photos.data] = ExposedBlob(bytes)
            it[Photos.contentType] = contentType
            it[Photos.taskId] = taskId
        }.value
    }

    /**
     * Returns the photo bytes and MIME type, optionally resized to [maxWidth].
     * Returns null if the photo does not exist.
     */
    suspend fun getPhoto(photoId: Long, maxWidth: Int? = null): Pair<ByteArray, String>? = dbQuery {
        val row = Photos.selectAll().where { Photos.id eq photoId }.singleOrNull()
            ?: return@dbQuery null

        val originalBytes = row[Photos.data].inputStream.readBytes()
        val contentType = row[Photos.contentType]

        if (maxWidth != null) {
            val resized = tryResize(originalBytes, maxWidth)
            if (resized != null) return@dbQuery Pair(resized, "image/jpeg")
        }

        Pair(originalBytes, contentType)
    }

    private fun tryResize(bytes: ByteArray, maxWidth: Int): ByteArray? {
        return try {
            val image: BufferedImage = ImageIO.read(ByteArrayInputStream(bytes)) ?: return null
            if (image.width <= maxWidth) return null
            val resized = Scalr.resize(image, Scalr.Method.ULTRA_QUALITY, Scalr.Mode.FIT_TO_WIDTH, maxWidth)
            val out = ByteArrayOutputStream()
            ImageIO.write(resized, "jpg", out)
            out.toByteArray()
        } catch (_: Exception) {
            null
        }
    }
}
