package com.example.data

import com.example.data.model.ParsedSmsResult
import com.example.data.model.TransactionType
import java.util.regex.Pattern

object SmsAiParser {

    /**
     * Parse SMS using Regex keywords and patterns for Iranian banks.
     */
    fun parseSmsWithRegex(smsText: String): ParsedSmsResult {
        val normalizedText = smsText.replace("ی", "ی").replace("ک", "ک")
        
        // 1. Transaction Type
        val incomeKeywords = listOf("واریز", "افزایش", "طلبکار", "به حساب", "به کارت", "انتقال به", "بازگشت", "سود", "پایا به")
        val expenseKeywords = listOf("برداشت", "خرید", "کسر", "بدهکار", "از حساب", "از کارت", "انتقال از", "پرداخت")

        val isIncome = incomeKeywords.any { normalizedText.contains(it) }
        val isExpense = expenseKeywords.any { normalizedText.contains(it) }

        // 2. Extract Amount
        val amountPattern = Pattern.compile("(?:مبلغ|مبلغ:|مبلغ :|واریز:|برداشت:)?\\s*([+-])?\\s*([0-9,٫]+)\\s*([+-])?\\s*(ریال|تومان|Rial)?")
        val amountMatcher = amountPattern.matcher(normalizedText)
        
        var amountToman = 0L
        var explicitSignType: TransactionType? = null

        if (amountMatcher.find()) {
            val signBefore = amountMatcher.group(1)
            val rawNum = amountMatcher.group(2)?.replace(",", "")?.replace("٫", "") ?: "0"
            val signAfter = amountMatcher.group(3)
            val unit = amountMatcher.group(4) ?: "ریال"

            if (signBefore == "+" || signAfter == "+") {
                explicitSignType = TransactionType.INCOME
            } else if (signBefore == "-" || signAfter == "-") {
                explicitSignType = TransactionType.EXPENSE
            }

            val parsedVal = rawNum.toLongOrNull() ?: 0L
            amountToman = if (unit.contains("تومان") || unit.contains("Toman", ignoreCase = true)) {
                parsedVal
            } else {
                parsedVal / 10
            }
        }

        val type = explicitSignType ?: if (isIncome && !isExpense) {
            TransactionType.INCOME
        } else if (isExpense && !isIncome) {
            TransactionType.EXPENSE
        } else if (isIncome) {
            if (normalizedText.contains("واریز")) TransactionType.INCOME else TransactionType.EXPENSE
        } else {
            TransactionType.EXPENSE
        }

        // 3. Extract Balance
        val balancePattern = Pattern.compile("موجودی(?:\\s*:|:)?\\s*([0-9,٫]+)\\s*(ریال|تومان)?")
        val balanceMatcher = balancePattern.matcher(normalizedText)
        var balanceToman: Long? = null
        if (balanceMatcher.find()) {
            val rawBal = balanceMatcher.group(1)?.replace(",", "")?.replace("٫", "") ?: "0"
            val balUnit = balanceMatcher.group(2) ?: "ریال"
            val parsedBal = rawBal.toLongOrNull() ?: 0L
            balanceToman = if (balUnit.contains("تومان")) parsedBal else parsedBal / 10
        }

        // 4. Extract Date / Time
        val timePattern = Pattern.compile("(\\d{2}:\\d{2}(?::\\d{2})?)")
        val timeMatcher = timePattern.matcher(normalizedText)
        var transactionTime: String? = null
        if (timeMatcher.find()) {
            transactionTime = timeMatcher.group(1)
        }

        // 5. Extract Card Number
        val cardMatcher = Pattern.compile("(\\d{4}\\*+\\d{4}|کارت\\s*\\d{4}|\\d{4}-\\d{4}|\\*\\d{4})")
        val cardNumberMatcher = cardMatcher.matcher(normalizedText)
        val cardNumber = if (cardNumberMatcher.find()) cardNumberMatcher.group(1)?.replace("-", "")?.replace("کارت", "")?.trim() else null

        // 6. Bank Name
        val bankName = when {
            normalizedText.contains("ملی") || normalizedText.contains("Melli") -> "بانک ملی"
            normalizedText.contains("ملت") || normalizedText.contains("Mellat") -> "بانک ملت"
            normalizedText.contains("پاسارگاد") || normalizedText.contains("Pasargad") -> "بانک پاسارگاد"
            normalizedText.contains("سامان") || normalizedText.contains("Saman") -> "بانک سامان"
            normalizedText.contains("بلوبانک") || normalizedText.contains("بلو") || normalizedText.contains("blu") -> "بلوبانک"
            normalizedText.contains("کشاورزی") -> "بانک کشاورزی"
            normalizedText.contains("تجارت") -> "بانک تجارت"
            normalizedText.contains("رسالت") -> "بانک رسالت"
            else -> "بانک نامشخص"
        }

        // 7. Merchant and Smart Category Matcher
        val categoryDictionary = mapOf(
            "حمل و نقل" to listOf("اسنپ", "تپسی", "ماکسیم", "قطار", "بلیط", "هواپیما", "snapp", "tapsi", "مترو"),
            "خوراکی و سوپرمارکت" to listOf("هایپر", "سوپر", "کافه", "رستوران", "نان", "شیرینی", "افق کوروش", "جانبو", "میوه"),
            "پوشاک و خرید" to listOf("دیجی‌کالا", "digikala", "فروشگاه", "پوشاک", "لباس", "کفش", "کیف"),
            "سلامت و درمان" to listOf("داروخانه", "بیمارستان", "مطب", "دکتر", "آزمایشگاه", "درمانگاه"),
            "قبوض و خدمات" to listOf("شارژ", "همراه اول", "ایرانسل", "رایتل", "برق", "آب", "گاز", "تلفن", "قبض"),
            "درآمد و حقوق" to listOf("حقوق", "دستمزد", "پاداش", "سود"),
        )

        var merchant = if (type == TransactionType.INCOME) "واریزکننده نامشخص" else "فروشگاه/پایانه نامشخص"
        var category = if (type == TransactionType.INCOME) "درآمد و حقوق" else "متفرقه"

        for ((cat, keywords) in categoryDictionary) {
            val matchedKeyword = keywords.find { normalizedText.contains(it, ignoreCase = true) }
            if (matchedKeyword != null) {
                category = cat
                merchant = matchedKeyword // A simple guess for the merchant name based on keyword
                break
            }
        }

        return ParsedSmsResult(
            amountToman = if (amountToman > 0) amountToman else 0L,
            type = type,
            merchantName = merchant,
            categoryName = category,
            cardNumber = cardNumber,
            bankName = bankName,
            rawText = smsText,
            balanceToman = balanceToman,
            transactionTime = transactionTime,
            confidence = 0.8f,
            parsedByAi = false
        )
    }

    suspend fun parseSms(smsText: String, apiKey: String = ""): ParsedSmsResult {
        return parseSmsWithRegex(smsText)
    }
}
