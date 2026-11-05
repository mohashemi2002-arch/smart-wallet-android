package com.example.data

import com.example.data.model.TransactionType
import org.junit.Assert.*
import org.junit.Test

class IranianBankSmsParserTest {

    @Test
    fun testBankSaderatSmsFormat1() {
        val sms = """
            پايانه فروش: 1,200,000-
            حساب:80001
            مانده:72,455,724
            0802 - 04:24
        """.trimIndent()

        val parsed = SmsAiParser.parseSmsWithRegex(sms, "BankSaderat")

        assertEquals(120_000L, parsed.amountToman)
        assertEquals(TransactionType.EXPENSE, parsed.type)
        assertEquals("80001", parsed.accountNumber)
        assertEquals(7_245_572L, parsed.balanceToman)
        assertEquals("بانک صادرات", parsed.bankName)
        assertFalse(SmsAiParser.isOtpSms(sms))
    }

    @Test
    fun testBankSaderatSmsFormat2() {
        val sms = """
            بانك صادرات ايران
            3,008,000-
            حساب:80001
            مانده:65,797,724
            0803 - 09:37
        """.trimIndent()

        val parsed = SmsAiParser.parseSmsWithRegex(sms, "BankSaderat")

        assertEquals(300_800L, parsed.amountToman)
        assertEquals(TransactionType.EXPENSE, parsed.type)
        assertEquals("80001", parsed.accountNumber)
        assertEquals(6_579_772L, parsed.balanceToman)
        assertEquals("بانک صادرات", parsed.bankName)
        assertFalse(SmsAiParser.isOtpSms(sms))
    }

    @Test
    fun testBankMehrIranExpenseSms() {
        val sms = """
            640101018243027001
            20,014,500-
            1405/7/6-20:18
            مانده: 373,462
        """.trimIndent()

        val parsed = SmsAiParser.parseSmsWithRegex(sms, "B.QMEHRIRAN")

        assertEquals(2_001_450L, parsed.amountToman)
        assertEquals(TransactionType.EXPENSE, parsed.type)
        assertEquals("640101018243027001", parsed.accountNumber)
        assertEquals(37_346L, parsed.balanceToman)
        assertEquals("بانک قرض‌الحسنه مهر ایران", parsed.bankName)
        assertFalse(SmsAiParser.isOtpSms(sms))
    }

    @Test
    fun testBankMehrIranIncomeSms() {
        val sms = """
            640101018243027001
            15,000,000+
            1405/7/7-14:28
            مانده:15,373,462
        """.trimIndent()

        val parsed = SmsAiParser.parseSmsWithRegex(sms, "B.QMEHRIRAN")

        assertEquals(1_500_000L, parsed.amountToman)
        assertEquals(TransactionType.INCOME, parsed.type)
        assertEquals("640101018243027001", parsed.accountNumber)
        assertEquals(1_537_346L, parsed.balanceToman)
        assertEquals("بانک قرض‌الحسنه مهر ایران", parsed.bankName)
        assertFalse(SmsAiParser.isOtpSms(sms))
    }

    @Test
    fun testBankMehrIranOtpFiltered() {
        val otpSms = """
            انتقال به 1389*603769
            20,000,000 ریال
            رمز 776830
            اعتبار 20:20:38
        """.trimIndent()

        assertTrue("OTP SMS should be detected as OTP", SmsAiParser.isOtpSms(otpSms))
        val parsed = SmsAiParser.parseSmsWithRegex(otpSms, "B.QMEHRIRAN")
        assertEquals(0L, parsed.amountToman)
    }

    @Test
    fun testBankMelliExpenseSms() {
        val sms = """
            بانك ملي ايران
            انتقال:2,991,000-
            حساب:02003
            مانده: 68,004
            0719-12:37
        """.trimIndent()

        val parsed = SmsAiParser.parseSmsWithRegex(sms, "09830009417")

        assertEquals(299_100L, parsed.amountToman)
        assertEquals(TransactionType.EXPENSE, parsed.type)
        assertEquals("02003", parsed.accountNumber)
        assertEquals(6_800L, parsed.balanceToman)
        assertEquals("بانک ملی", parsed.bankName)
    }

    @Test
    fun testBankMelliIncomeSubsidySms() {
        val sms = """
            بانك ملي ايران
            يارانه:3,000,000+
            حساب:02003
            مانده:3,068,009
            0730-17:23
        """.trimIndent()

        val parsed = SmsAiParser.parseSmsWithRegex(sms, "09830009417")

        assertEquals(300_000L, parsed.amountToman)
        assertEquals(TransactionType.INCOME, parsed.type)
        assertEquals("02003", parsed.accountNumber)
        assertEquals(306_800L, parsed.balanceToman)
        assertEquals("بانک ملی", parsed.bankName)
    }

    @Test
    fun testBankMelliIncomePoleTransferSms() {
        val sms = """
            بانك ملي ايران
            حواله پل:50,000,000+
            حساب:02003
            مانده:53,068,009
            0730-18:21
        """.trimIndent()

        val parsed = SmsAiParser.parseSmsWithRegex(sms, "09830009417")

        assertEquals(5_000_000L, parsed.amountToman)
        assertEquals(TransactionType.INCOME, parsed.type)
        assertEquals("02003", parsed.accountNumber)
        assertEquals(5_306_800L, parsed.balanceToman)
        assertEquals("بانک ملی", parsed.bankName)
    }

    @Test
    fun testBankSaderatMosaedehAndDateParsing() {
        val sms = """
            مساعده: 158,472,000+
            حساب:80001
            مانده:219,118,075
            15:38 - 0813
        """.trimIndent()

        val parsed = SmsAiParser.parseSmsWithRegex(sms, "BankSaderat")

        // Crucial: Must parse 15,847,200 Toman, NOT 81 Toman!
        assertEquals(15_847_200L, parsed.amountToman)
        assertEquals(TransactionType.INCOME, parsed.type)
        assertEquals("80001", parsed.accountNumber)
        assertEquals(21_911_807L, parsed.balanceToman)
        assertEquals("بانک صادرات", parsed.bankName)
        assertNotNull(parsed.dateMillis)
    }

    @Test
    fun testBankSaderatInternetPurchaseAndDateParsing() {
        val sms = """
            خريد اينترنتي: 200,000,000-
            حساب:80001
            مانده: 109,903,780
            07:09 - 0628
        """.trimIndent()

        val parsed = SmsAiParser.parseSmsWithRegex(sms, "BankSaderat")

        // Crucial: Must parse 20,000,000 Toman, NOT 62 Toman!
        assertEquals(20_000_000L, parsed.amountToman)
        assertEquals(TransactionType.EXPENSE, parsed.type)
        assertEquals("80001", parsed.accountNumber)
        assertEquals(10_990_378L, parsed.balanceToman)
        assertEquals("بانک صادرات", parsed.bankName)
        assertNotNull(parsed.dateMillis)
    }

    @Test
    fun testBankSaderatTransferPlusFee() {
        val sms = """
            انتقال+کارمزد: 2,511,000-
            حساب:80001
            مانده: 326,528,780
            11:10 - 0625
        """.trimIndent()

        val parsed = SmsAiParser.parseSmsWithRegex(sms, "BankSaderat")

        // Crucial: Must parse 251,100 Toman, NOT 62 Toman!
        assertEquals(251_100L, parsed.amountToman)
        assertEquals(TransactionType.EXPENSE, parsed.type)
        assertEquals("80001", parsed.accountNumber)
        assertEquals(32_652_878L, parsed.balanceToman)
        assertEquals("بانک صادرات", parsed.bankName)
        assertNotNull(parsed.dateMillis)
    }

    @Test
    fun testBankSaderatStandaloneAmount() {
        val sms = """
            بانك صادرات ايران
            23,008,000-
            حساب:80001
            مانده:193,560,075
            0814 - 00:16
        """.trimIndent()

        val parsed = SmsAiParser.parseSmsWithRegex(sms, "BankSaderat")

        assertEquals(2_300_800L, parsed.amountToman)
        assertEquals(TransactionType.EXPENSE, parsed.type)
        assertEquals("80001", parsed.accountNumber)
        assertEquals(19_356_007L, parsed.balanceToman)
        assertEquals("بانک صادرات", parsed.bankName)
        assertNotNull(parsed.dateMillis)
    }

    @Test
    fun testBankMelliPayaIncome() {
        val sms = """
            بانك ملي ايران
            پايا:400,000,000+
            حساب:02003
            مانده:400,094,974
            0505-13:07
        """.trimIndent()

        val parsed = SmsAiParser.parseSmsWithRegex(sms, "09830009417")

        assertEquals(40_000_000L, parsed.amountToman)
        assertEquals(TransactionType.INCOME, parsed.type)
        assertEquals("02003", parsed.accountNumber)
        assertEquals(40_009_497L, parsed.balanceToman)
        assertEquals("بانک ملی", parsed.bankName)
        assertNotNull(parsed.dateMillis)
    }
}
