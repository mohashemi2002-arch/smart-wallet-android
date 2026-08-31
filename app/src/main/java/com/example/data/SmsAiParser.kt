package com.example.data

import android.util.Log
import com.example.data.model.ParsedSmsResult
import com.example.data.model.TransactionType
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

object SmsAiParser {

    private const val TAG = "SmsAiParser"

    /**
     * AI Sub-Prompt designed for Gemini API to extract structured financial data from Iranian bank SMS.
     */
    const val GEMINI_SYSTEM_PROMPT = """
You are an expert Iranian Bank SMS Parser for the Poolaki AI personal finance app.
Your task is to analyze raw SMS messages sent by Iranian banks (e.g., Melli, Mellat, Pasargad, Saman, Blue Bank, Keshavarzi, Sepah, Parsian) and extract precise transaction details in strict JSON format.

RULES:
1. Identify if the transaction is an EXPENSE (برداشت / خرید / انتقال از / بدهکار / کسر) or INCOME (واریز / به حساب / طلبکار / افزایش).
2. Extract the amount in TOMAN. Note: Most Iranian bank SMS report amounts in RIAL (Rials). You MUST convert RIAL to TOMAN by dividing by 10 (e.g. 1,500,000 Rials = 150,000 Toman). If the SMS explicitly states 'تومان' or 'Toman', use that value directly.
3. Extract the merchant name, store name, or recipient if available (e.g., "Hyperstar", "Snapp", "Cafe Bazaar", "Kourosh"). If unknown, set merchantName to the bank name or "خرید عمومی".
4. Determine the best categoryName from this list:
   - "خوراکی و سوپرمارکت" (Food & Grocery)
   - "حمل و نقل" (Transport & Snapp)
   - "مسکن و اجاره" (Housing & Rent)
   - "قبوض و خدمات" (Bills & Utilities)
   - "سلامت و درمان" (Healthcare)
   - "پوشاک و خرید" (Shopping)
   - "تفریح و سرگرمی" (Entertainment & Dining)
   - "پس‌انداز و سرمایه" (Savings & Investment)
   - "متفرقه" (General / Other)
5. Extract the card number mask (e.g. "6037****1234") or bank name if present.
6. Output MUST be ONLY a valid JSON object without markdown codeblocks or extra text.

JSON SCHEMA:
{
  "amountToman": number,
  "transactionType": "EXPENSE" | "INCOME",
  "merchantName": string,
  "categoryName": string,
  "cardNumber": string,
  "bankName": string,
  "confidence": number
}
"""

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    /**
     * Parse SMS using Gemini AI with fallback to Regex.
     */
    suspend fun parseSms(smsText: String, apiKey: String = BuildConfig.GEMINI_API_KEY): ParsedSmsResult {
        return withContext(Dispatchers.IO) {
            if (apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY") {
                try {
                    val aiResult = callGeminiApi(smsText, apiKey)
                    if (aiResult != null) {
                        return@withContext aiResult
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Gemini parsing failed, using regex fallback: ${e.message}")
                }
            }
            // Fallback to local regex parser
            parseSmsWithRegex(smsText)
        }
    }

    private fun callGeminiApi(smsText: String, apiKey: String): ParsedSmsResult? {
        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"

        val jsonPayload = JSONObject().apply {
            put("systemInstruction", JSONObject().apply {
                put("parts", JSONObject().put("text", GEMINI_SYSTEM_PROMPT))
            })
            put("contents", JSONObject().put("parts", JSONObject().put("text", "Parse this Iranian Bank SMS:\n$smsText")))
            put("generationConfig", JSONObject().apply {
                put("responseMimeType", "application/json")
                put("temperature", 0.1)
            })
        }

        val mediaType = "application/json; charset=utf-8".toMediaType()
        val request = Request.Builder()
            .url(url)
            .post(jsonPayload.toString().toRequestBody(mediaType))
            .build()

        val response = httpClient.newCall(request).execute()
        if (!response.isSuccessful) {
            Log.w(TAG, "Gemini HTTP error: ${response.code}")
            return null
        }

        val responseBody = response.body?.string() ?: return null
        val responseJson = JSONObject(responseBody)
        val candidates = responseJson.optJSONArray("candidates") ?: return null
        if (candidates.length() == 0) return null

        val text = candidates.getJSONObject(0)
            .getJSONObject("content")
            .getJSONArray("parts")
            .getJSONObject(0)
            .getString("text")

        val parsedObj = JSONObject(text.trim())

        val amountToman = parsedObj.optLong("amountToman", 0L)
        val typeStr = parsedObj.optString("transactionType", "EXPENSE")
        val type = if (typeStr.equals("INCOME", ignoreCase = true)) TransactionType.INCOME else TransactionType.EXPENSE
        val merchant = parsedObj.optString("merchantName", "خرید فروشگاهی")
        val category = parsedObj.optString("categoryName", "خوراکی و سوپرمارکت")
        val cardNum = parsedObj.optString("cardNumber", "")
        val bankName = parsedObj.optString("bankName", "بانک")

        return ParsedSmsResult(
            amountToman = amountToman,
            type = type,
            merchantName = merchant,
            categoryName = category,
            cardNumber = if (cardNum.isNotBlank()) cardNum else null,
            bankName = if (bankName.isNotBlank()) bankName else null,
            rawText = smsText,
            confidence = parsedObj.optDouble("confidence", 0.95).toFloat(),
            parsedByAi = true
        )
    }

    /**
     * Robust local Regex Parser for Iranian Bank SMS messages (Offline Fallback).
     */
    fun parseSmsWithRegex(smsText: String): ParsedSmsResult {
        var isIncome = smsText.contains("واریز") || smsText.contains("افزایش") || smsText.contains("طلبکار")
        val isExpense = smsText.contains("برداشت") || smsText.contains("خرید") || smsText.contains("کسر") || smsText.contains("بدهکار")

        val type = if (isIncome && !isExpense) TransactionType.INCOME else TransactionType.EXPENSE

        // Extract amount numbers
        val amountPattern = Pattern.compile("(?:مبلغ|مبلغ:|مبلغ :|واریز:|برداشت:)?\\s*([0-9,٫]+)\\s*(ریال|تومان)?")
        val matcher = amountPattern.matcher(smsText)

        var amountToman = 0L
        if (matcher.find()) {
            val rawNum = matcher.group(1)?.replace(",", "")?.replace("٫", "") ?: "0"
            val unit = matcher.group(2) ?: "ریال"
            val parsedVal = rawNum.toLongOrNull() ?: 0L

            amountToman = if (unit.contains("تومان")) {
                parsedVal
            } else {
                parsedVal / 10 // Convert Rial to Toman
            }
        }

        // If no amount matched with unit, search for first large number
        if (amountToman == 0L) {
            val digitsOnly = smsText.replace(Regex("[^0-9]"), " ")
            val nums = digitsOnly.split("\\s+".toRegex()).mapNotNull { it.toLongOrNull() }
            val potentialAmount = nums.firstOrNull { it >= 1000 }
            if (potentialAmount != null) {
                amountToman = potentialAmount / 10
            }
        }

        // Bank detection
        val bankName = when {
            smsText.contains("ملی") || smsText.contains("Melli") -> "بانک ملی"
            smsText.contains("ملت") || smsText.contains("Mellat") -> "بانک ملت"
            smsText.contains("صادرات") -> "بانک صادرات"
            smsText.contains("پاسارگاد") -> "بانک پاسارگاد"
            smsText.contains("سامان") -> "بانک سامان"
            smsText.contains("بلوبانک") || smsText.contains("بلو") || smsText.contains("blue") -> "بلوبانک"
            smsText.contains("کشاورزی") -> "بانک کشاورزی"
            smsText.contains("سپه") -> "بانک سپه"
            smsText.contains("تجارت") -> "بانک تجارت"
            smsText.contains("پارسیان") -> "بانک پارسیان"
            else -> "بانک"
        }

        // Card number extraction (e.g. 6037****1234 or کارت 1234)
        val cardMatcher = Pattern.compile("(\\d{4}\\*+\\d{4}|کارت\\s*\\d{4})").matcher(smsText)
        val cardNumber = if (cardMatcher.find()) cardMatcher.group(1) else null

        // Merchant and category guessing
        val merchant = when {
            smsText.contains("اسنپ") || smsText.contains("Snapp") -> "اسنپ"
            smsText.contains("دیجی‌کالا") || smsText.contains("Digikala") -> "دیجی‌کالا"
            smsText.contains("کافه") || smsText.contains("Cafe") -> "کافه / رستوران"
            smsText.contains("هایپر") || smsText.contains("Hyper") || smsText.contains("افق") -> "هایپرمارکت"
            smsText.contains("شارژ") || smsText.contains("همراه اول") || smsText.contains("ایرانسل") -> "شارژ موبایل"
            smsText.contains("تپسی") || smsText.contains("Tapsi") -> "تپسی"
            smsText.contains("داروخانه") -> "داروخانه"
            else -> if (type == TransactionType.INCOME) "واریزکننده" else "خرید کارت‌خوان"
        }

        val category = when {
            merchant.contains("اسنپ") || merchant.contains("تپسی") -> "حمل و نقل"
            merchant.contains("هایپر") || merchant.contains("کافه") -> "خوراکی و سوپرمارکت"
            merchant.contains("داروخانه") -> "سلامت و درمان"
            merchant.contains("دیجی‌کالا") || merchant.contains("شارژ") -> "پوشاک و خرید"
            type == TransactionType.INCOME -> "درآمد و حقوق"
            else -> "متفرقه"
        }

        return ParsedSmsResult(
            amountToman = if (amountToman > 0) amountToman else 50000L,
            type = type,
            merchantName = merchant,
            categoryName = category,
            cardNumber = cardNumber,
            bankName = bankName,
            rawText = smsText,
            confidence = 0.75f,
            parsedByAi = false
        )
    }
}
