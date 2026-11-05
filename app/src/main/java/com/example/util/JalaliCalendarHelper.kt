package com.example.util

import java.util.Calendar

data class JalaliDate(
    val year: Int,
    val month: Int,
    val day: Int,
    val monthName: String,
    val dayOfWeekName: String
)

object JalaliCalendarHelper {
    val monthNames = arrayOf(
        "فروردین", "اردیبهشت", "خرداد",
        "تیر", "مرداد", "شهریور",
        "مهر", "آبان", "آذر",
        "دی", "بهمن", "اسفند"
    )

    val weekDayNames = arrayOf(
        "یکشنبه", "دوشنبه", "سه‌شنبه", "چهارشنبه", "پنج‌شنبه", "جمعه", "شنبه"
    )

    fun gregorianToJalali(gYear: Int, gMonth: Int, gDay: Int): Triple<Int, Int, Int> {
        val gDaysInMonth = intArrayOf(31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
        val jDaysInMonth = intArrayOf(31, 31, 31, 31, 31, 31, 30, 30, 30, 30, 30, 29)

        var gy = gYear - 1600
        var gm = gMonth - 1
        var gd = gDay - 1

        var gDayNo = 365 * gy + (gy + 3) / 4 - (gy + 99) / 100 + (gy + 399) / 400

        for (i in 0 until gm) {
            gDayNo += gDaysInMonth[i]
        }
        if (gm > 1 && ((gy % 4 == 0 && gy % 100 != 0) || (gy % 400 == 0))) {
            gDayNo++
        }
        gDayNo += gd

        var jDayNo = gDayNo - 79
        val jNp = jDayNo / 12053
        jDayNo %= 12053

        var jy = 979 + 33 * jNp + 4 * (jDayNo / 1461)
        jDayNo %= 1461

        if (jDayNo >= 366) {
            jy += (jDayNo - 1) / 365
            jDayNo = (jDayNo - 1) % 365
        }

        var jm = 0
        while (jm < 11 && jDayNo >= jDaysInMonth[jm]) {
            jDayNo -= jDaysInMonth[jm]
            jm++
        }
        return Triple(jy, jm + 1, jDayNo + 1)
    }

    fun jalaliToGregorian(jYear: Int, jMonth: Int, jDay: Int): Triple<Int, Int, Int> {
        var jy = jYear - 979
        var jm = jMonth - 1
        var jd = jDay - 1

        var jDayNo = 365 * jy + (jy / 33) * 8 + (jy % 33 + 3) / 4
        for (i in 0 until jm) {
            jDayNo += if (i < 6) 31 else 30
        }
        jDayNo += jd

        var gDayNo = jDayNo + 79
        var gy = 1600 + 400 * (gDayNo / 146097)
        gDayNo %= 146097

        var leap = true
        if (gDayNo >= 36525) {
            gDayNo--
            gy += 100 * (gDayNo / 36524)
            gDayNo %= 36524
            if (gDayNo >= 365) {
                gDayNo++
            } else {
                leap = false
            }
        }

        gy += 4 * (gDayNo / 1461)
        gDayNo %= 1461

        if (gDayNo >= 366) {
            leap = false
            gDayNo--
            gy += gDayNo / 365
            gDayNo %= 365
        }

        val gDaysInMonth = intArrayOf(31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
        if (leap) gDaysInMonth[1] = 29

        var gm = 0
        while (gm < 11 && gDayNo >= gDaysInMonth[gm]) {
            gDayNo -= gDaysInMonth[gm]
            gm++
        }

        return Triple(gy, gm + 1, gDayNo + 1)
    }

    fun getJalaliDateFromMillis(millis: Long): JalaliDate {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = millis
        val gYear = calendar.get(Calendar.YEAR)
        val gMonth = calendar.get(Calendar.MONTH) + 1
        val gDay = calendar.get(Calendar.DAY_OF_MONTH)
        val (jYear, jMonth, jDay) = gregorianToJalali(gYear, gMonth, gDay)
        
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        // Calendar.DAY_OF_WEEK: Sunday=1, Monday=2, ..., Saturday=7
        // Our array: Sunday=0, Monday=1, ..., Saturday=6
        val dayName = weekDayNames[dayOfWeek - 1]

        return JalaliDate(
            year = jYear,
            month = jMonth,
            day = jDay,
            monthName = monthNames[jMonth - 1],
            dayOfWeekName = dayName
        )
    }
    
    fun getMillisFromJalali(jYear: Int, jMonth: Int, jDay: Int, hour: Int = 0, minute: Int = 0, second: Int = 0): Long {
        val (gYear, gMonth, gDay) = jalaliToGregorian(jYear, jMonth, jDay)
        val cal = Calendar.getInstance()
        cal.set(gYear, gMonth - 1, gDay, hour, minute, second)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    fun getStartAndEndOfJalaliDay(jYear: Int, jMonth: Int, jDay: Int): Pair<Long, Long> {
        val start = getMillisFromJalali(jYear, jMonth, jDay, 0, 0, 0)
        val end = getMillisFromJalali(jYear, jMonth, jDay, 23, 59, 59)
        return Pair(start, end)
    }

    fun getStartAndEndOfJalaliMonth(jYear: Int, jMonth: Int): Pair<Long, Long> {
        val start = getMillisFromJalali(jYear, jMonth, 1, 0, 0, 0)
        val length = if (jMonth <= 6) 31 else if (jMonth <= 11) 30 else {
            val leap = isLeapYear(jYear)
            if (leap) 30 else 29
        }
        val end = getMillisFromJalali(jYear, jMonth, length, 23, 59, 59)
        return Pair(start, end)
    }
    
    fun isLeapYear(jYear: Int): Boolean {
        val a = jYear - 474
        val b = (a % 2820 + 2820) % 2820
        return (b * 682 - 110) % 2816 < 682
    }
    
    fun getMonthLength(jYear: Int, jMonth: Int): Int {
        if (jMonth <= 6) return 31
        if (jMonth <= 11) return 30
        return if (isLeapYear(jYear)) 30 else 29
    }

    fun formatToIsoDateTime(gYear: Int, gMonth: Int, gDay: Int, hour: Int, minute: Int, second: Int = 0): String {
        return String.format(java.util.Locale.US, "%04d-%02d-%02dT%02d:%02d:%02d", gYear, gMonth, gDay, hour, minute, second)
    }

    fun getIsoStringFromMillis(millis: Long): String {
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.US)
        return sdf.format(java.util.Date(millis))
    }

    /**
     * Parses bank SMS date and time from Iranian formats:
     * - MMDD-HH:MM (e.g. 0730-17:23, 0803-15:28) - Month: 07, Day: 30
     * - MMDD - HH:MM (e.g. 0814 - 00:16, 0813 - 21:06) - Month: 08, Day: 14
     * - HH:MM - MMDD (e.g. 15:38 - 0813, 11:10 - 0625, 07:09 - 0628) - Month: 08, Day: 13
     * - Explicit: 1403/07/30 17:23 or تاریخ: 1405/03/31 ساعت: 08:39:00
     * - Compact: 14050330-23:19:50
     *
     * Standardizes all formats into full ISO 8601 date string (YYYY-MM-DDTHH:mm:ss) and epoch millis.
     */
    fun parseBankSmsDateDetails(smsText: String, fallbackMillis: Long = System.currentTimeMillis()): ParsedBankDateResult? {
        val norm = SmsNormalizer.normalize(smsText)
            .replace('–', '-')
            .replace('—', '-')
            .replace('−', '-')
        val currentJalali = getJalaliDateFromMillis(fallbackMillis)
        val currentYear = currentJalali.year

        try {
            // 1. Explicit full Jalali Date: 140x/MM/DD with optional time
            val fullDateRegex = Regex("""(?:تاريخ|تاریخ)?\s*:?\s*(1[34]\d{2})[/-](0[1-9]|1[0-2])[/-](0[1-9]|[12]\d|3[01])""")
            val fullDateMatch = fullDateRegex.find(norm)
            if (fullDateMatch != null) {
                val y = fullDateMatch.groupValues[1].toInt()
                val m = fullDateMatch.groupValues[2].toInt()
                val d = fullDateMatch.groupValues[3].toInt()
                val timeRegex = Regex("""(?:ساعت)?\s*:?\s*(\d{2}):(\d{2})(?::(\d{2}))?""")
                val timeMatch = timeRegex.find(norm)
                val h = timeMatch?.groupValues?.getOrNull(1)?.toIntOrNull() ?: 12
                val min = timeMatch?.groupValues?.getOrNull(2)?.toIntOrNull() ?: 0
                val s = timeMatch?.groupValues?.getOrNull(3)?.toIntOrNull() ?: 0
                val (gy, gm, gd) = jalaliToGregorian(y, m, d)
                val millis = getMillisFromJalali(y, m, d, h, min, s)
                val isoStr = formatToIsoDateTime(gy, gm, gd, h, min, s)
                return ParsedBankDateResult(
                    jalaliYear = y,
                    jalaliMonth = m,
                    jalaliDay = d,
                    hour = h,
                    minute = min,
                    second = s,
                    gregorianYear = gy,
                    gregorianMonth = gm,
                    gregorianDay = gd,
                    epochMillis = millis,
                    isoDateTime = isoStr
                )
            }

            // 2. Compact 14-digit Jalali timestamp: 14050330-23:19:50
            val compactRegex = Regex("""(1[34]\d{2})(0[1-9]|1[0-2])([0-3]\d)-(\d{2}):(\d{2}):?(\d{2})?""")
            val compactMatch = compactRegex.find(norm)
            if (compactMatch != null) {
                val y = compactMatch.groupValues[1].toInt()
                val m = compactMatch.groupValues[2].toInt()
                val d = compactMatch.groupValues[3].toInt()
                val h = compactMatch.groupValues[4].toInt()
                val min = compactMatch.groupValues[5].toInt()
                val s = compactMatch.groupValues.getOrNull(6)?.toIntOrNull() ?: 0
                val (gy, gm, gd) = jalaliToGregorian(y, m, d)
                val millis = getMillisFromJalali(y, m, d, h, min, s)
                val isoStr = formatToIsoDateTime(gy, gm, gd, h, min, s)
                return ParsedBankDateResult(
                    jalaliYear = y,
                    jalaliMonth = m,
                    jalaliDay = d,
                    hour = h,
                    minute = min,
                    second = s,
                    gregorianYear = gy,
                    gregorianMonth = gm,
                    gregorianDay = gd,
                    epochMillis = millis,
                    isoDateTime = isoStr
                )
            }

            // 3. HH:MM - MMDD (e.g. 15:38 - 0813, 11:10 - 0625, 07:09 - 0628)
            val timeFirstRegex = Regex("""(\d{2}):(\d{2})\s*-\s*(0[1-9]|1[0-2])([0-3]\d)""")
            val timeFirstMatch = timeFirstRegex.find(norm)
            if (timeFirstMatch != null) {
                val h = timeFirstMatch.groupValues[1].toInt()
                val min = timeFirstMatch.groupValues[2].toInt()
                val m = timeFirstMatch.groupValues[3].toInt() // e.g. 08 -> 8th Jalali month
                val d = timeFirstMatch.groupValues[4].toInt() // e.g. 13 -> 13th day
                val y = if (m > currentJalali.month + 1) currentYear - 1 else currentYear
                val (gy, gm, gd) = jalaliToGregorian(y, m, d)
                val millis = getMillisFromJalali(y, m, d, h, min, 0)
                val isoStr = formatToIsoDateTime(gy, gm, gd, h, min, 0)
                return ParsedBankDateResult(
                    jalaliYear = y,
                    jalaliMonth = m,
                    jalaliDay = d,
                    hour = h,
                    minute = min,
                    second = 0,
                    gregorianYear = gy,
                    gregorianMonth = gm,
                    gregorianDay = gd,
                    epochMillis = millis,
                    isoDateTime = isoStr
                )
            }

            // 4. MMDD - HH:MM or MMDD-HH:MM (e.g. 0730-17:23, 0814 - 00:16, 0803-15:28)
            val dateFirstRegex = Regex("""(0[1-9]|1[0-2])([0-3]\d)\s*-\s*(\d{2}):(\d{2})(?::(\d{2}))?""")
            val dateFirstMatch = dateFirstRegex.find(norm)
            if (dateFirstMatch != null) {
                val m = dateFirstMatch.groupValues[1].toInt() // e.g. 07 -> 7th Jalali month
                val d = dateFirstMatch.groupValues[2].toInt() // e.g. 30 -> 30th day
                val h = dateFirstMatch.groupValues[3].toInt()
                val min = dateFirstMatch.groupValues[4].toInt()
                val s = dateFirstMatch.groupValues.getOrNull(5)?.toIntOrNull() ?: 0
                val y = if (m > currentJalali.month + 1) currentYear - 1 else currentYear
                val (gy, gm, gd) = jalaliToGregorian(y, m, d)
                val millis = getMillisFromJalali(y, m, d, h, min, s)
                val isoStr = formatToIsoDateTime(gy, gm, gd, h, min, s)
                return ParsedBankDateResult(
                    jalaliYear = y,
                    jalaliMonth = m,
                    jalaliDay = d,
                    hour = h,
                    minute = min,
                    second = s,
                    gregorianYear = gy,
                    gregorianMonth = gm,
                    gregorianDay = gd,
                    epochMillis = millis,
                    isoDateTime = isoStr
                )
            }
        } catch (e: Exception) {
            // Ignore format error and fallback
        }

        return null
    }

    fun parseBankSmsDateTime(smsText: String, fallbackMillis: Long = System.currentTimeMillis()): Long? {
        return parseBankSmsDateDetails(smsText, fallbackMillis)?.epochMillis
    }
}

data class ParsedBankDateResult(
    val jalaliYear: Int,
    val jalaliMonth: Int,
    val jalaliDay: Int,
    val hour: Int,
    val minute: Int,
    val second: Int = 0,
    val gregorianYear: Int,
    val gregorianMonth: Int,
    val gregorianDay: Int,
    val epochMillis: Long,
    val isoDateTime: String
)
