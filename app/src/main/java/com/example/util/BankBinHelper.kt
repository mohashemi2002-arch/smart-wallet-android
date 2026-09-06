package com.example.util

data class BankInfo(val name: String, val colorHex: String)

object BankBinHelper {
    private val binMap = mapOf(
        "603769" to BankInfo("بانک صادرات ایران", "#1B2A4A"),
        "903769" to BankInfo("بانک صادرات ایران", "#1B2A4A"),
        "603799" to BankInfo("بانک ملی ایران", "#1565C0"),
        "170019" to BankInfo("بانک ملی ایران", "#1565C0"),
        "603770" to BankInfo("بانک کشاورزی", "#2E7D32"),
        "639217" to BankInfo("بانک کشاورزی", "#2E7D32"),
        "610433" to BankInfo("بانک ملت", "#C2185B"),
        "991975" to BankInfo("بانک ملت", "#C2185B"),
        "585983" to BankInfo("بانک تجارت", "#00838F"),
        "627353" to BankInfo("بانک تجارت", "#00838F"),
        "589210" to BankInfo("بانک سپه", "#D84315"),
        "627381" to BankInfo("بانک سپه (انصار)", "#D84315"),
        "639599" to BankInfo("بانک سپه (قوامین)", "#D84315"),
        "636949" to BankInfo("بانک سپه (حکمت ایرانیان)", "#D84315"),
        "639370" to BankInfo("بانک سپه (مهر اقتصاد)", "#D84315"),
        "505801" to BankInfo("بانک سپه (کوثر)", "#D84315"),
        "589463" to BankInfo("بانک رفاه کارگران", "#0D47A1"),
        "628023" to BankInfo("بانک مسکن", "#E65100"),
        "621986" to BankInfo("بانک سامان / بلوبانک", "#00ACC1"),
        "502229" to BankInfo("بانک پاسارگاد", "#FBC02D"),
        "639347" to BankInfo("بانک پاسارگاد", "#FBC02D"),
        "622106" to BankInfo("بانک پارسیان", "#7B1FA2"),
        "639194" to BankInfo("بانک پارسیان", "#7B1FA2"),
        "627884" to BankInfo("بانک پارسیان", "#7B1FA2"),
        "502806" to BankInfo("بانک شهر", "#D32F2F"),
        "504706" to BankInfo("بانک شهر", "#D32F2F"),
        "636214" to BankInfo("بانک آینده", "#5D4037"),
        "502938" to BankInfo("بانک دی", "#00897B"),
        "639346" to BankInfo("بانک سینا", "#558B2F"),
        "627412" to BankInfo("بانک اقتصاد نوین", "#4527A0"),
        "627488" to BankInfo("بانک کارآفرین", "#37474F"),
        "502908" to BankInfo("بانک توسعه تعاون / کارآفرین", "#0288D1"),
        "639607" to BankInfo("بانک سرمایه", "#1976D2"),
        "505416" to BankInfo("بانک گردشگری", "#C62828"),
        "505426" to BankInfo("بانک گردشگری", "#C62828"),
        "505785" to BankInfo("بانک ایران زمین", "#689F38"),
        "585947" to BankInfo("بانک خاورمیانه", "#424242"),
        "504172" to BankInfo("بانک قرض‌الحسنه رسالت", "#00695C"),
        "606373" to BankInfo("بانک قرض‌الحسنه مهر ایران", "#2E7D32"),
        "627760" to BankInfo("پست بانک ایران", "#388E3C"),
        "207177" to BankInfo("بانک توسعه تعاون", "#0288D1"),
        "627961" to BankInfo("بانک صنعت و معدن", "#795548"),
        "627648" to BankInfo("بانک توسعه صادرات ایران", "#1A237E"),
        "636795" to BankInfo("بانک مرکزی", "#212121"),
        "606256" to BankInfo("مؤسسه اعتباری ملل", "#009688"),
        "507677" to BankInfo("مؤسسه اعتباری نور", "#E91E63"),
        "581874" to BankInfo("مؤسسه اعتباری کاسپین", "#607D8B")
    )

    private fun normalizePersianDigits(input: String): String {
        val persianDigits = arrayOf("۰", "۱", "۲", "۳", "۴", "۵", "۶", "۷", "۸", "۹")
        val arabicDigits = arrayOf("٠", "١", "٢", "٣", "٤", "٥", "٦", "٧", "٨", "٩")
        var result = input
        for (i in 0..9) {
            result = result.replace(persianDigits[i], i.toString())
            result = result.replace(arabicDigits[i], i.toString())
        }
        return result
    }

    fun getBankInfoFromCardNumber(cardNumber: String): BankInfo? {
        val normalized = normalizePersianDigits(cardNumber).replace("-", "").replace(" ", "")
        if (normalized.length >= 6) {
            val prefix = normalized.substring(0, 6)
            return binMap[prefix]
        }
        return null
    }
    
    fun getAllUniqueBanks(): List<String> {
        return binMap.values.map { it.name }.distinct().sorted()
    }
}
