package com.example.ui.components

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

class ThousandsSeparatorVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        // Strip out existing commas to be safe (if any somehow got in)
        val originalText = text.text
        val digitsOnly = originalText.filter { it.isDigit() || it in '۰'..'۹' }

        if (digitsOnly.isEmpty()) {
            return TransformedText(text, OffsetMapping.Identity)
        }

        // We want to format the digits with 3-digit commas.
        // Let's use the toPersianDigits mechanism to ensure it's displayed fully Persian.
        val formatted = formatWithCommas(digitsOnly)
        val finalPersianFormatted = PersianCurrencyFormatter.toPersianDigits(formatted)

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                if (offset <= 0) return 0
                val validOffset = offset.coerceAtMost(digitsOnly.length)
                
                var transformedOffset = 0
                var originalCount = 0
                for (i in finalPersianFormatted.indices) {
                    if (originalCount == validOffset) break
                    if (finalPersianFormatted[i] != '،' && finalPersianFormatted[i] != ',') {
                        originalCount++
                    }
                    transformedOffset++
                }
                return transformedOffset
            }

            override fun transformedToOriginal(offset: Int): Int {
                if (offset <= 0) return 0
                val validOffset = offset.coerceAtMost(finalPersianFormatted.length)
                
                var originalOffset = 0
                for (i in 0 until validOffset) {
                    if (finalPersianFormatted[i] != '،' && finalPersianFormatted[i] != ',') {
                        originalOffset++
                    }
                }
                return originalOffset
            }
        }

        return TransformedText(AnnotatedString(finalPersianFormatted), offsetMapping)
    }

    private fun formatWithCommas(text: String): String {
        val reversed = text.reversed()
        val chunked = reversed.chunked(3)
        // You can use standard comma ',' or Persian comma '،'. The prompt uses '،' or ',' depending on context,
        // Let's stick to standard comma ',' which `DecimalFormat` uses, then replace with '،' if needed, or just let DecimalFormat format.
        // The prompt says "2,000,000 (or with Persian commas ۲،۰۰۰،۰۰۰)"
        // Since we are formatting explicitly, we'll use '،' for better Persian UX.
        return chunked.joinToString("،").reversed()
    }
}
