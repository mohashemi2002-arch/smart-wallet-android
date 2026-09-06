package com.example.util

object SmsNormalizer {
    fun normalize(text: String): String {
        var normalized = text
        normalized = normalized.replace("ك", "ک")
        normalized = normalized.replace("ي", "ی")
        
        val arabicDigits = charArrayOf('٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩')
        val persianDigits = charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')
        
        for (i in 0..9) {
            normalized = normalized.replace(arabicDigits[i], i.toString()[0])
            normalized = normalized.replace(persianDigits[i], i.toString()[0])
        }
        return normalized
    }
}
