package ca.cem.ktormyb.security

import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

/**
 * AES/ECB encryption for task names, matching the original KickMyB-Server behaviour.
 * The key can be overridden via the ENCRYPTION_KEY environment variable.
 * NOTE: store the key outside source control in production.
 */
object Encryptor {
    private val key: ByteArray = run {
        val raw = (System.getenv("ENCRYPTION_KEY") ?: "secret-key-12345")
            .toByteArray(Charsets.UTF_8)
        // AES requires exactly 16, 24, or 32 bytes
        raw.copyOf(16)
    }
    private val secretKey = SecretKeySpec(key, "AES")

    fun encrypt(value: String): String {
        val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        return Base64.getEncoder().encodeToString(cipher.doFinal(value.toByteArray(Charsets.UTF_8)))
    }

    fun decrypt(value: String): String = try {
        val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
        cipher.init(Cipher.DECRYPT_MODE, secretKey)
        String(cipher.doFinal(Base64.getDecoder().decode(value)), Charsets.UTF_8)
    } catch (_: Exception) {
        value // return as-is if the value was never encrypted
    }
}
