import re

with open('app/src/main/java/com/example/ui/components/JalaliCalendarDialog.kt', 'r') as f:
    content = f.read()

target1 = '''            when (initialFilter) {
                is TransactionDateFilter.DayFilter -> {
                    viewedYear = initialFilter.jYear
                    viewedMonth = initialFilter.jMonth
                }
                is TransactionDateFilter.MonthFilter -> {
                    viewedYear = initialFilter.jYear
                    viewedMonth = initialFilter.jMonth
                }
            }'''
            
replacement1 = '''            when (initialFilter) {
                is TransactionDateFilter.DayFilter -> {
                    viewedYear = initialFilter.jYear
                    viewedMonth = initialFilter.jMonth
                }
                is TransactionDateFilter.MonthFilter -> {
                    viewedYear = initialFilter.jYear
                    viewedMonth = initialFilter.jMonth
                }
                is TransactionDateFilter.AllTime -> {}
            }'''

content = content.replace(target1, replacement1)

target2 = '''                val displayTitle = when (val filter = selectedFilter) {
                    is TransactionDateFilter.DayFilter -> {
                        if (filter.jYear == currentJalali.year && filter.jMonth == currentJalali.month && filter.jDay == currentJalali.day) {
                            "امروز - ${filter.jDay} ${JalaliCalendarHelper.monthNames[filter.jMonth - 1]}"
                        } else {
                            val tempMillis = JalaliCalendarHelper.getMillisFromJalali(filter.jYear, filter.jMonth, filter.jDay)
                            val d = JalaliCalendarHelper.getJalaliDateFromMillis(tempMillis)
                            "${d.dayOfWeekName} ${d.day} ${d.monthName} ${d.year}"
                        }
                    }
                    is TransactionDateFilter.MonthFilter -> "${JalaliCalendarHelper.monthNames[filter.jMonth - 1]} ${filter.jYear}"
                    null -> "انتخاب تاریخ تراکنش‌ها"
                }'''

replacement2 = '''                val displayTitle = when (val filter = selectedFilter) {
                    is TransactionDateFilter.DayFilter -> {
                        if (filter.jYear == currentJalali.year && filter.jMonth == currentJalali.month && filter.jDay == currentJalali.day) {
                            "امروز - ${filter.jDay} ${JalaliCalendarHelper.monthNames[filter.jMonth - 1]}"
                        } else {
                            val tempMillis = JalaliCalendarHelper.getMillisFromJalali(filter.jYear, filter.jMonth, filter.jDay)
                            val d = JalaliCalendarHelper.getJalaliDateFromMillis(tempMillis)
                            "${d.dayOfWeekName} ${d.day} ${d.monthName} ${d.year}"
                        }
                    }
                    is TransactionDateFilter.MonthFilter -> "${JalaliCalendarHelper.monthNames[filter.jMonth - 1]} ${filter.jYear}"
                    is TransactionDateFilter.AllTime -> "کل تراکنش‌ها"
                    null -> "انتخاب تاریخ تراکنش‌ها"
                }'''
                
content = content.replace(target2, replacement2)

target3 = '''                        val isSelected = when (val filter = selectedFilter) {
                            is TransactionDateFilter.DayFilter -> filter.jYear == viewedYear && filter.jMonth == viewedMonth && filter.jDay == day
                            is TransactionDateFilter.MonthFilter -> false
                            null -> false
                        }'''

replacement3 = '''                        val isSelected = when (val filter = selectedFilter) {
                            is TransactionDateFilter.DayFilter -> filter.jYear == viewedYear && filter.jMonth == viewedMonth && filter.jDay == day
                            is TransactionDateFilter.MonthFilter -> false
                            is TransactionDateFilter.AllTime -> false
                            null -> false
                        }'''

content = content.replace(target3, replacement3)

with open('app/src/main/java/com/example/ui/components/JalaliCalendarDialog.kt', 'w') as f:
    f.write(content)

