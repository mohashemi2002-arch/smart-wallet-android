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
}
