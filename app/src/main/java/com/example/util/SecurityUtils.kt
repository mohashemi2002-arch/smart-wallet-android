package com.example.util

/**
 * Security & privacy utilities for financial PII data masking.
 */
object SecurityUtils {

    /**
     * Masks sensitive numbers, card details, phone numbers, and amounts with asterisks (*).
     *
     * Examples:
     * - Card numbers: "6037997112345678" -> "6037********5678"
     * - Currency amounts: "1,009,000" -> "*******"
     * - Phone numbers: "09123456789" -> "0912****789"
     */
    fun maskSensitiveData(input: String?): String {
        if (input.isNullOrBlank()) return ""

        // 1. Mask 16-digit bank cards: keep first 4 and last 4
        var result = input.replace(Regex("\\b(\\d{4})\\d{8,11}(\\d{4})\\b")) { match ->
            val first = match.groupValues[1]
            val last = match.groupValues[2]
            "$first********$last"
        }

        // 2. Mask 3-digit comma-separated monetary amounts
        result = result.replace(Regex("\\b\\d{1,3}(?:[,،٫]\\d{3})+\\b")) { match ->
            "*".repeat(match.value.length)
        }

        // 3. Mask Iranian mobile phone numbers: keep first 4 and last 3
        result = result.replace(Regex("\\b(09\\d{2})\\d{4}(\\d{3})\\b")) { match ->
            "${match.groupValues[1]}****${match.groupValues[2]}"
        }

        return result
    }
}

/**
 * Extension function for safe logging without exposing financial PII.
 */
fun String.maskSensitiveData(): String = SecurityUtils.maskSensitiveData(this)
