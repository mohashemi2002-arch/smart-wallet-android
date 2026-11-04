package com.example.domain.util

import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.Mac
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

/**
 * Standard cryptographic utility for secure password/PIN hashing,
 * verification, constant-time comparisons, and API request signing.
 *
 * Replaces insecure static-salt SHA-256 with PBKDF2-HMAC-SHA256 with
 * per-credential cryptographically secure random salts.
 */
object PasswordHasher {

    const val DEFAULT_ITERATIONS = 10000
    private const val KEY_LENGTH_BITS = 256
    private const val SALT_BYTE_SIZE = 16
    private const val ALGORITHM = "PBKDF2WithHmacSHA256"
    private const val PREFIX = "pbkdf2"

    // Legacy static salt preserved STRICTLY for seamless one-time migration of existing users
    private const val LEGACY_STATIC_SALT = "SmartWallet_Auth_Salt_2026_Secured#"

    private val secureRandom = SecureRandom()

    /**
     * Hashes a secret (Password or PIN) using PBKDF2-HMAC-SHA256 with a unique random salt.
     * Output format: pbkdf2$<iterations>$<saltHex>$<hashHex>
     */
    fun hash(secret: String, iterations: Int = DEFAULT_ITERATIONS): String {
        val salt = ByteArray(SALT_BYTE_SIZE)
        secureRandom.nextBytes(salt)
        return hashWithSalt(secret, salt, iterations)
    }

    private fun hashWithSalt(secret: String, salt: ByteArray, iterations: Int): String {
        val spec = PBEKeySpec(secret.toCharArray(), salt, iterations, KEY_LENGTH_BITS)
        val factory = SecretKeyFactory.getInstance(ALGORITHM)
        val hashBytes = factory.generateSecret(spec).encoded
        spec.clearPassword()

        val saltHex = salt.joinToString("") { "%02x".format(it) }
        val hashHex = hashBytes.joinToString("") { "%02x".format(it) }
        return "$PREFIX$$iterations$$saltHex$$hashHex"
    }

    /**
     * Verifies a secret against a stored hash using constant-time comparison.
     * Supports both modern PBKDF2 format and legacy SHA-256 format for backward compatibility.
     */
    fun verify(secret: String, storedHash: String?): Boolean {
        if (storedHash.isNullOrBlank()) return false

        return if (storedHash.startsWith("$PREFIX$")) {
            verifyPbkdf2(secret, storedHash)
        } else {
            // Legacy SHA-256 verification
            verifyLegacySha256(secret, storedHash)
        }
    }

    /**
     * Returns true if the stored hash was created with the legacy algorithm
     * and should be upgraded to PBKDF2 immediately.
     */
    fun isLegacyHash(storedHash: String?): Boolean {
        if (storedHash.isNullOrBlank()) return false
        return !storedHash.startsWith("$PREFIX$")
    }

    private fun verifyPbkdf2(secret: String, storedHash: String): Boolean {
        val parts = storedHash.split("$")
        if (parts.size != 4 || parts[0] != PREFIX) return false

        val iterations = parts[1].toIntOrNull() ?: return false
        val saltHex = parts[2]
        val expectedHashHex = parts[3]

        val salt = hexToBytes(saltHex) ?: return false
        val spec = PBEKeySpec(secret.toCharArray(), salt, iterations, KEY_LENGTH_BITS)
        val factory = SecretKeyFactory.getInstance(ALGORITHM)
        val actualHashBytes = factory.generateSecret(spec).encoded
        spec.clearPassword()

        val expectedHashBytes = hexToBytes(expectedHashHex) ?: return false
        return MessageDigest.isEqual(actualHashBytes, expectedHashBytes)
    }

    private fun verifyLegacySha256(secret: String, storedHash: String): Boolean {
        val md = MessageDigest.getInstance("SHA-256")
        val computedBytes = md.digest((LEGACY_STATIC_SALT + secret).toByteArray(Charsets.UTF_8))
        val expectedBytes = hexToBytes(storedHash) ?: return false
        return MessageDigest.isEqual(computedBytes, expectedBytes)
    }

    /**
     * Legacy SHA-256 hash generator used exclusively for migration checks.
     */
    fun hashLegacySha256(input: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val bytes = md.digest((LEGACY_STATIC_SALT + input).toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    /**
     * Generates a time-bound HMAC-SHA256 signature for authenticating Cloud Sync & Restore requests.
     */
    fun computeHmacSha256(data: String, key: String): String {
        val mac = Mac.getInstance("HmacSHA256")
        val secretKey = SecretKeySpec(key.toByteArray(Charsets.UTF_8), "HmacSHA256")
        mac.init(secretKey)
        val rawHmac = mac.doFinal(data.toByteArray(Charsets.UTF_8))
        return rawHmac.joinToString("") { "%02x".format(it) }
    }

    private fun hexToBytes(hex: String): ByteArray? {
        if (hex.length % 2 != 0) return null
        return try {
            ByteArray(hex.length / 2) { i ->
                hex.substring(i * 2, i * 2 + 2).toInt(16).toByte()
            }
        } catch (_: Exception) {
            null
        }
    }
}
