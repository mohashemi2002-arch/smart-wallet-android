package com.example.data

import com.example.data.model.ParsedSmsResult
import com.example.data.model.TransactionType
import com.example.util.SmsNormalizer
import java.util.regex.Pattern

object SmsAiParser {

    fun parseSmsWithRegex(smsText: String, senderAddress: String? = null): ParsedSmsResult {
        val normalizedText = SmsNormalizer.normalize(smsText)
        
        var amountToman = 0L
        var balanceToman: Long? = null
        var explicitSignType: TransactionType? = null
        var cardNumber: String? = null
        
        // Check for Bank Melli specific format
        val isMelliSender = senderAddress != null && (senderAddress.contains("30009417") || senderAddress.contains("09830009417"))
        val isMelliText = normalizedText.contains("بانک ملی") || normalizedText.contains("Melli") || normalizedText.contains("بانک ملي")
        val isMelli = isMelliSender || isMelliText
        
        if (isMelli) {
            // Melli Amount pattern: انتقال:1,459,000- or +2,000,000
            val melliAmountPattern = Pattern.compile("(?:انتقال|برداشت|واریز|خرید|مبلغ)?\\s*:?\\s*([+-])?\\s*([0-9,٫]+)\\s*([+-])?")
            val melliAmountMatcher = melliAmountPattern.matcher(normalizedText)
            if (melliAmountMatcher.find()) {
                val signBefore = melliAmountMatcher.group(1)
                val rawNum = melliAmountMatcher.group(2)?.replace(",", "")?.replace("٫", "") ?: "0"
                val signAfter = melliAmountMatcher.group(3)
                
                if (signBefore == "+" || signAfter == "+") explicitSignType = TransactionType.INCOME
                else if (signBefore == "-" || signAfter == "-") explicitSignType = TransactionType.EXPENSE
                
                // Melli is always in Rial, divide by 10
                amountToman = (rawNum.toLongOrNull() ?: 0L) / 10
            }
            
            // Melli Balance pattern: مانده:82,000
            val melliBalancePattern = Pattern.compile("مانده\\s*:?\\s*([0-9,٫]+)")
            val melliBalanceMatcher = melliBalancePattern.matcher(normalizedText)
            if (melliBalanceMatcher.find()) {
                val rawBal = melliBalanceMatcher.group(1)?.replace(",", "")?.replace("٫", "") ?: "0"
                balanceToman = (rawBal.toLongOrNull() ?: 0L) / 10
            }
            
            // Melli Account pattern: حساب:02003
            val melliAccountPattern = Pattern.compile("حساب\\s*:?\\s*(\\d+)")
            val melliAccountMatcher = melliAccountPattern.matcher(normalizedText)
            if (melliAccountMatcher.find()) {
                cardNumber = melliAccountMatcher.group(1)
            }
        }
        
        // General parsing if not found by Melli specifics or to augment
        if (amountToman == 0L) {
            val amountPattern = Pattern.compile("(?:مبلغ|مبلغ:|مبلغ :|واریز:|برداشت:)?\\s*([+-])?\\s*([0-9,٫]+)\\s*([+-])?\\s*(ریال|تومان|Rial)?")
            val amountMatcher = amountPattern.matcher(normalizedText)
                    
            if (amountMatcher.find()) {
                val signBefore = amountMatcher.group(1)
                val rawNum = amountMatcher.group(2)?.replace(",", "")?.replace("٫", "") ?: "0"
                val signAfter = amountMatcher.group(3)
                val unit = amountMatcher.group(4) ?: "ریال"
                
                if (signBefore == "+" || signAfter == "+") explicitSignType = TransactionType.INCOME
                else if (signBefore == "-" || signAfter == "-") explicitSignType = TransactionType.EXPENSE
                
                val parsedVal = rawNum.toLongOrNull() ?: 0L
                amountToman = if (unit.contains("تومان") || unit.contains("Toman", ignoreCase = true)) {
                    parsedVal
                } else {
                    parsedVal / 10
                }
            }
        }
        
        if (balanceToman == null) {
            val balancePattern = Pattern.compile("موجودی(?:\\s*:|:)?\\s*([0-9,٫]+)\\s*(ریال|تومان)?")
            val balanceMatcher = balancePattern.matcher(normalizedText)
            if (balanceMatcher.find()) {
                val rawBal = balanceMatcher.group(1)?.replace(",", "")?.replace("٫", "") ?: "0"
                val balUnit = balanceMatcher.group(2) ?: "ریال"
                val parsedBal = rawBal.toLongOrNull() ?: 0L
                balanceToman = if (balUnit.contains("تومان")) parsedBal else parsedBal / 10
            }
        }

        if (cardNumber == null) {
            val cardMatcher = Pattern.compile("(\\d{4}\\*+\\d{4}|کارت\\s*\\d{4}|\\d{4}-\\d{4}|\\*\\d{4})")
            val cardNumberMatcher = cardMatcher.matcher(normalizedText)
            cardNumber = if (cardNumberMatcher.find()) cardNumberMatcher.group(1)?.replace("-", "")?.replace("کارت", "")?.trim() else null
        }

        val incomeKeywords = listOf("واریز", "افزایش", "طلبکار", "به حساب", "به کارت", "انتقال به", "بازگشت", "سود", "پایا به")
        val expenseKeywords = listOf("برداشت", "خرید", "کسر", "بدهکار", "از حساب", "از کارت", "انتقال از", "پرداخت")
        val isIncome = incomeKeywords.any { normalizedText.contains(it) }
        val isExpense = expenseKeywords.any { normalizedText.contains(it) }

        val type = explicitSignType ?: if (isIncome && !isExpense) {
            TransactionType.INCOME
        } else if (isExpense && !isIncome) {
            TransactionType.EXPENSE
        } else if (isIncome) {
            if (normalizedText.contains("واریز")) TransactionType.INCOME else TransactionType.EXPENSE
        } else {
            TransactionType.EXPENSE
        }

        val timePattern = Pattern.compile("(\\d{2}:\\d{2}(?::\\d{2})?)")
        val timeMatcher = timePattern.matcher(normalizedText)
        var transactionTime: String? = null
        if (timeMatcher.find()) {
            transactionTime = timeMatcher.group(1)
        }

        val bankName = when {
            isMelli -> "بانک ملی"
            normalizedText.contains("ملت") || normalizedText.contains("Mellat") -> "بانک ملت"
            normalizedText.contains("پاسارگاد") || normalizedText.contains("Pasargad") -> "بانک پاسارگاد"
            normalizedText.contains("سامان") || normalizedText.contains("Saman") -> "بانک سامان"
            normalizedText.contains("بلوبانک") || normalizedText.contains("بلو") || normalizedText.contains("blu") -> "بلوبانک"
            normalizedText.contains("کشاورزی") -> "بانک کشاورزی"
            normalizedText.contains("تجارت") -> "بانک تجارت"
            normalizedText.contains("رسالت") -> "بانک رسالت"
            else -> "بانک نامشخص"
        }

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
                merchant = matchedKeyword 
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

    suspend fun parseSms(smsText: String, apiKey: String = "", senderAddress: String? = null): ParsedSmsResult {
        return parseSmsWithRegex(smsText, senderAddress)
    }
}
