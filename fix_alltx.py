import re

with open('app/src/main/java/com/example/ui/components/AllTransactionsDialog.kt', 'r') as f:
    content = f.read()

# Make sure JalaliCalendarHelper is imported
if "import com.example.util.JalaliCalendarHelper" not in content:
    content = content.replace("import androidx.compose.runtime.*", "import androidx.compose.runtime.*\nimport com.example.util.JalaliCalendarHelper")

# Fix grouping
grouping_target = '''    // Group transactions by Date string (e.g., "جمعه ، ۹ مرداد ۱۴۰۵")
    // For simplicity, we just format the date.
    val grouped = remember(transactions) {
        transactions.groupBy { tx ->
            val sdf = java.text.SimpleDateFormat("yyyy/MM/dd", java.util.Locale.US)
            sdf.format(java.util.Date(tx.dateMillis))
        }
    }'''

grouping_replacement = '''    // Group transactions by Date string
    val grouped = remember(transactions) {
        transactions.groupBy { tx ->
            val jalali = JalaliCalendarHelper.getJalaliDateFromMillis(tx.dateMillis)
            "${jalali.dayOfWeekName} ، ${jalali.day} ${jalali.monthName} ${jalali.year}"
        }
    }'''

content = content.replace(grouping_target, grouping_replacement)

# Fix the mock title
mock_target = '''                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "مرداد ۱۴۰۵", // Mock for now
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Color.White)
                            }'''

mock_replacement = '''                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "همه تراکنش‌ها",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                            }'''

content = content.replace(mock_target, mock_replacement)

with open('app/src/main/java/com/example/ui/components/AllTransactionsDialog.kt', 'w') as f:
    f.write(content)
