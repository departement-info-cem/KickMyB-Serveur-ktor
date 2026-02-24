package ca.cem.ktormyb.config

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Custom serializer for epoch milliseconds to ISO 8601 format string.
 * Converts Long (epoch millis) to "yyyy-MM-dd'T'HH:mm:ss" format and vice versa.
 */
object EpochMillisToIsoStringSerializer : KSerializer<Long> {
    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
        .withZone(ZoneId.systemDefault())

    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor(
        "EpochMillisToIsoString",
        PrimitiveKind.STRING
    )

    override fun serialize(encoder: Encoder, value: Long) {
        val instant = Instant.ofEpochMilli(value)
        val isoString = formatter.format(instant)
        encoder.encodeString(isoString)
    }

    override fun deserialize(decoder: Decoder): Long {
        val isoString = decoder.decodeString()
        val dateTime = LocalDateTime.parse(isoString, formatter)
        val instant = dateTime.atZone(ZoneId.systemDefault()).toInstant()
        return instant.toEpochMilli()
    }
}

