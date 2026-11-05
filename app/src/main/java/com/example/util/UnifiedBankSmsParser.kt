package com.example.util

import com.example.data.model.ParsedSmsResult
import com.example.data.model.TransactionType
import java.util.regex.Pattern

/**
 * Unified SMS parser for Iranian banking formats, specifically tailored for:
 * 1. Bank Melli Iran (بانک ملی ایران)
 * 2. Bank Saderat Iran (بانک صادرات ایران)
 * Also supporting general Iranian banking formats (Mehr, Mellat, Pasargad, Saman, Blu, etc.).
 *
 * Special features:
 * - Accurately interprets MMDD formats (e.g. '0730' as 7th Jalali month, 30th day).
 * - Standardizes to a full ISO 8601 date format (e.g. "2024-10-21T17:23:00").
 * - Robust against negative date numbers (-0628, -0813) misinterpreting as transaction amounts.
 * - Handles all specialized keywords: مساعده, پایانه‌فروش, خرید اینترنتی, انتقال+کارمزد, شرح آزاد, واریز حقوق, یارانه, حواله پل, پایا.
 */
object UnifiedBankSmsParser {

    /**
     * Extracts and standardizes bank date from SMS text.
     * Guaranteed to correctly interpret 'MMDD' (e.g. '0730' -> Month: 7, Day: 30)
     * and produce a standardized ISO 8601 date string.
     */
    fun parseBankDate(smsText: String, fallbackMillis: Long = System.currentTimeMillis()): ParsedBankDateResult? {
        return JalaliCalendarHelper.parseBankSmsDateDetails(smsText, fallbackMillis)
    }

    /**
     * Unified parsing logic for bank SMS messages.
     */
    fun parse(
        smsText: String,
        senderAddress: String? = null,
        fallbackMillis: Long = System.currentTimeMillis()
    ): ParsedSmsResult {
        val normalizedText = SmsNormalizer.normalize(smsText)

        // 1. Check for OTP / Dynamic Password SMS
        val hasOtpKeyword = normalizedText.contains("رمز") ||
                normalizedText.contains("اعتبار") ||
                normalizedText.contains("کد تایید") ||
                normalizedText.contains("کد تأیید") ||
                normalizedText.contains("رمز پویا") ||
                normalizedText.contains("رمز یکبار مصرف")
        val hasBalance = normalizedText.contains("مانده") || normalizedText.contains("موجودی")
        if (hasOtpKeyword && !hasBalance) {
            return ParsedSmsResult(
                amountToman = 0L,
                type = TransactionType.EXPENSE,
                merchantName = "پیامک رمز پویا (OTP)",
                categoryName = "رمز پویا",
                rawText = smsText,
                confidence = 0.0f,
                parsedByAi = false
            )
        }

        var amountToman = 0L
        var balanceToman: Long? = null
        var explicitSignType: TransactionType? = null
        var cardNumber: String? = null
        var accountNumber: String? = null

        // Detect Banks
        val isMelliSender = senderAddress != null && (senderAddress.contains("30009417") || senderAddress.contains("09830009417") || senderAddress.contains("30009411"))
        val isMelliText = normalizedText.contains("بانک ملی") || normalizedText.contains("Melli") || normalizedText.contains("بانک ملي") || normalizedText.contains("بانك ملي")
        val isMelli = isMelliSender || isMelliText

        val isSaderatSender = senderAddress != null && (senderAddress.contains("BankSaderat", ignoreCase = true) || senderAddress.contains("saderat", ignoreCase = true))
        val isSaderatText = normalizedText.contains("صادرات") || normalizedText.contains("Saderat", ignoreCase = true)
        val isSaderat = isSaderatSender || isSaderatText

        val isMehrSender = senderAddress != null && (senderAddress.contains("QMEHR", ignoreCase = true) || senderAddress.contains("MEHR", ignoreCase = true) || senderAddress.contains("qmehr724", ignoreCase = true))
        val isMehrText = normalizedText.contains("مهر ایران") || normalizedText.contains("قرض‌الحسنه مهر") || normalizedText.contains("qmehr724") || normalizedText.lines().any { it.trim().startsWith("640101") }
        val isMehr = isMehrSender || isMehrText

        // 2. Extract Account Number (شماره حساب)
        val accountLabelPattern = Pattern.compile("حساب\\s*:?\\s*(\\d+)")
        val accountMatcher = accountLabelPattern.matcher(normalizedText)
        if (accountMatcher.find()) {
            accountNumber = accountMatcher.group(1)?.trim()
        } else {
            // Standalone account number in line (e.g. Bank Mehr 18-digit account number 640101018243027001)
            for (line in normalizedText.lines()) {
                val trimmed = line.trim()
                if (trimmed.matches(Regex("""^\d{13,20}$"""))) {
                    accountNumber = trimmed
                    break
                }
            }
        }

        // 3. Extract Card Number (شماره کارت)
        val cardMatcher = Pattern.compile("(?:کارت|كارت)?\\s*:?\\s*(\\d{4}\\*+\\d{4}|کارت\\s*\\d{4}|كارت\\s*\\d{4}|\\d{4}-\\d{4}|\\*\\d{4}|\\b\\d{16}\\b|\\b\\d{4}\\b)")
        val cardNumberMatcher = cardMatcher.matcher(normalizedText)
        if (cardNumberMatcher.find()) {
            val matchedCard = cardNumberMatcher.group(1)?.replace("-", "")?.replace("کارت", "")?.replace("كارت", "")?.replace(":", "")?.trim()
            if (matchedCard != null && matchedCard != accountNumber && !matchedCard.startsWith("140") && (accountNumber == null || !accountNumber.contains(matchedCard))) {
                cardNumber = matchedCard
            }
        }

        // 4. Extract Date Details and Standardize to ISO format
        val dateResult = parseBankDate(smsText, fallbackMillis)
        val dateMillis = dateResult?.epochMillis ?: fallbackMillis
        val isoDateTime = dateResult?.isoDateTime

        // 5. Extract Amount & Sign (+ or -)
        val labeledSignedPattern = Pattern.compile(
            "(?:مساعده|پايانه فروش|پایانه فروش|خرید اینترنتی|خريد اينترنتي|خريداينترنتي|خرید شارژ|خريد شارژ|" +
            "انتقال\\s*\\+\\s*کارمزد|انتقال|انتقالی|انتقالي|حواله پل|حواله پایا|پایا|پايا|ساتنا|شرح آزاد|" +
            "واریز حقوق|واريز حقوق|یارانه|يارانه|برداشت|واریز|واريز|خرید|خريد|مبلغ|اصلاحی|اصلاحي)" +
            "\\s*:?\\s*([+-])?\\s*([0-9,٫]+)\\s*([+-])?\\s*(ریال|تومان|Rial)?",
            Pattern.CASE_INSENSITIVE
        )
        val labeledMatcher = labeledSignedPattern.matcher(normalizedText)

        var standaloneMatchedGroup: MatchResult? = null
        val hasLabeledMatch = labeledMatcher.find()
        if (!hasLabeledMatch) {
            val lines = normalizedText.lines()
            for (line in lines) {
                val trimmed = line.trim()
                if (trimmed.contains(":") || trimmed.contains("حساب") || trimmed.contains("مانده") || trimmed.contains("موجودی") || trimmed.contains("کارت")) {
                    continue
                }
                val standaloneRegex = Regex("""^([+-])?\s*([0-9]{1,3}(?:,[0-9]{3})+|[0-9]{5,12})\s*([+-])?\s*(?:ریال|تومان|Rial)?$""")
                val m = standaloneRegex.find(trimmed)
                if (m != null) {
                    val sign1 = m.groupValues[1]
                    val sign2 = m.groupValues[3]
                    if (sign1.isNotEmpty() || sign2.isNotEmpty()) {
                        standaloneMatchedGroup = m
                        break
                    }
                }
            }
        }

        if (hasLabeledMatch) {
            val fullMatch = labeledMatcher.group(0) ?: ""
            val signBefore = labeledMatcher.group(1)
            val rawNum = labeledMatcher.group(2)?.replace(",", "")?.replace("٫", "") ?: "0"
            val signAfter = try { labeledMatcher.group(3) } catch (e: Exception) { null }
            val unit = try { labeledMatcher.group(4) ?: "ریال" } catch (e: Exception) { "ریال" }

            if (signBefore == "+" || signAfter == "+") {
                explicitSignType = TransactionType.INCOME
            } else if (signBefore == "-" || signAfter == "-") {
                explicitSignType = TransactionType.EXPENSE
            } else {
                if (fullMatch.contains("واریز") || fullMatch.contains("واريز") || fullMatch.contains("مساعده") || fullMatch.contains("یارانه") || fullMatch.contains("يارانه") || fullMatch.contains("حقوق") || fullMatch.contains("شرح آزاد")) {
                    explicitSignType = TransactionType.INCOME
                } else if (fullMatch.contains("برداشت") || fullMatch.contains("خرید") || fullMatch.contains("خريد") || fullMatch.contains("فروش") || fullMatch.contains("شارژ")) {
                    explicitSignType = TransactionType.EXPENSE
                }
            }

            val parsedVal = rawNum.toLongOrNull() ?: 0L
            amountToman = if (unit.contains("تومان") || unit.contains("Toman", ignoreCase = true)) {
                parsedVal
            } else {
                parsedVal / 10L
            }
        } else if (standaloneMatchedGroup != null) {
            val sign1 = standaloneMatchedGroup.groupValues[1]
            val rawNum = standaloneMatchedGroup.groupValues[2].replace(",", "").replace("٫", "")
            val sign2 = standaloneMatchedGroup.groupValues[3]

            if (sign1 == "+" || sign2 == "+") {
                explicitSignType = TransactionType.INCOME
            } else if (sign1 == "-" || sign2 == "-") {
                explicitSignType = TransactionType.EXPENSE
            }

            val parsedVal = rawNum.toLongOrNull() ?: 0L
            amountToman = parsedVal / 10L
        }

        // 6. Extract Balance (مانده حساب)
        val balancePattern = Pattern.compile("(?:موجودی|مانده)(?:\\s+(?:حساب|کارت|کل|فعلی|جدید|نهایی|شما))?(?:\\s*:|:)?\\s*([0-9,٫]+)\\s*(ریال|تومان)?")
        val balanceMatcher = balancePattern.matcher(normalizedText)
        if (balanceMatcher.find()) {
            val rawBal = balanceMatcher.group(1)?.replace(",", "")?.replace("٫", "") ?: "0"
            val balUnit = balanceMatcher.group(2) ?: "ریال"
            val parsedBal = rawBal.toLongOrNull() ?: 0L
            balanceToman = if (balUnit.contains("تومان")) parsedBal else parsedBal / 10L
        }

        // 7. Transaction Type
        val incomeKeywords = listOf("واریز", "افزایش", "طلبکار", "به حساب", "به کارت", "انتقال به", "بازگشت", "سود", "پایا به", "یارانه", "يارانه", "مساعده", "حقوق", "شرح آزاد")
        val expenseKeywords = listOf("برداشت", "خرید", "خريد", "کسر", "بدهکار", "از حساب", "از کارت", "انتقال از", "پرداخت", "پایانه فروش", "پايانه فروش")
        val hasIncomeWord = incomeKeywords.any { normalizedText.contains(it) }
        val hasExpenseWord = expenseKeywords.any { normalizedText.contains(it) }

        val type = explicitSignType ?: if (hasIncomeWord && !hasExpenseWord) {
            TransactionType.INCOME
        } else if (hasExpenseWord && !hasIncomeWord) {
            TransactionType.EXPENSE
        } else if (hasIncomeWord) {
            if (normalizedText.contains("واریز") || normalizedText.contains("یارانه") || normalizedText.contains("مساعده")) TransactionType.INCOME else TransactionType.EXPENSE
        } else {
            TransactionType.EXPENSE
        }

        // 8. Bank Name
        val bankName = when {
            isSaderat -> "بانک صادرات"
            isMelli -> "بانک ملی"
            isMehr -> "بانک قرض‌الحسنه مهر ایران"
            normalizedText.contains("ملت") || normalizedText.contains("Mellat") -> "بانک ملت"
            normalizedText.contains("پاسارگاد") || normalizedText.contains("Pasargad") -> "بانک پاسارگاد"
            normalizedText.contains("سامان") || normalizedText.contains("Saman") -> "بانک سامان"
            normalizedText.contains("بلوبانک") || normalizedText.contains("بلو") || normalizedText.contains("blu") -> "بلوبانک"
            normalizedText.contains("کشاورزی") -> "بانک کشاورزی"
            normalizedText.contains("تجارت") -> "بانک تجارت"
            normalizedText.contains("رسالت") -> "بانک رسالت"
            else -> "بانک نامشخص"
        }

        // 9. Merchant & Category
        val categoryDictionary = mapOf(
            "حمل و نقل" to listOf("اسنپ", "تپسی", "ماکسیم", "قطار", "بلیط", "هواپیما", "snapp", "tapsi", "مترو"),
            "خوراکی و سوپرمارکت" to listOf("هایپر", "سوپر", "کافه", "رستوران", "نان", "شیرینی", "افق کوروش", "جانبو", "میوه"),
            "پوشاک و خرید" to listOf("دیجی‌کالا", "digikala", "فروشگاه", "پوشاک", "لباس", "کفش", "کیف", "پایانه فروش", "پايانه فروش", "خرید اینترنتی", "خريد اينترنتي", "خريداينترنتي"),
            "سلامت و درمان" to listOf("داروخانه", "بیمارستان", "مطب", "دکتر", "آزمایشگاه", "درمانگاه"),
            "قبوض و خدمات" to listOf("شارژ", "همراه اول", "ایرانسل", "رایتل", "برق", "آب", "گاز", "تلفن", "قبض"),
            "درآمد و حقوق" to listOf("حقوق", "دستمزد", "پاداش", "سود", "یارانه", "يارانه", "مساعده"),
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

        val transactionTime = dateResult?.let {
            String.format(java.util.Locale.US, "%02d:%02d", it.hour, it.minute)
        }

        return ParsedSmsResult(
            amountToman = if (amountToman > 0) amountToman else 0L,
            type = type,
            merchantName = merchant,
            categoryName = category,
            cardNumber = cardNumber,
            accountNumber = accountNumber,
            bankName = bankName,
            rawText = smsText,
            balanceToman = balanceToman,
            transactionTime = transactionTime,
            dateMillis = dateMillis,
            isoDateTime = isoDateTime,
            confidence = 0.95f,
            parsedByAi = false
        )
    }
}
