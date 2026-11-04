package com.example.domain.util

import org.junit.Assert.*
import org.junit.Test

class PasswordHasherTest {

    @Test
    fun testPbkdf2HashAndVerifySuccess() {
        val pin = "1234"
        val hash = PasswordHasher.hash(pin)

        assertTrue("Hash must start with pbkdf2$", hash.startsWith("pbkdf2$"))
        assertTrue("PasswordHasher must verify correct PIN", PasswordHasher.verify(pin, hash))
        assertFalse("PasswordHasher must reject wrong PIN", PasswordHasher.verify("9999", hash))
        assertFalse("Modern hash must not be flagged as legacy", PasswordHasher.isLegacyHash(hash))
    }

    @Test
    fun testUniqueSaltsForSameInput() {
        val pin = "5678"
        val hash1 = PasswordHasher.hash(pin)
        val hash2 = PasswordHasher.hash(pin)

        assertNotEquals("Two hashes of same input must have different salts and values", hash1, hash2)
        assertTrue(PasswordHasher.verify(pin, hash1))
        assertTrue(PasswordHasher.verify(pin, hash2))
    }

    @Test
    fun testLegacySha256VerificationAndMigration() {
        val oldPin = "4321"
        val legacyHash = PasswordHasher.hashLegacySha256(oldPin)

        assertTrue("Legacy hash must be identified as needing upgrade", PasswordHasher.isLegacyHash(legacyHash))
        assertTrue("Legacy hash must be verified successfully for backward compatibility", PasswordHasher.verify(oldPin, legacyHash))
        assertFalse("Legacy verification must fail on wrong PIN", PasswordHasher.verify("0000", legacyHash))
    }

    @Test
    fun testHmacSha256Signature() {
        val message = "09121112233:restore:1730000000"
        val secret = "mySecretKey123"
        val sig1 = PasswordHasher.computeHmacSha256(message, secret)
        val sig2 = PasswordHasher.computeHmacSha256(message, secret)

        assertEquals("HMAC should be deterministic", sig1, sig2)
        assertEquals(64, sig1.length) // 256 bits = 64 hex characters
    }
}
