import re

with open('app/src/main/java/com/example/ui/components/Formatters.kt', 'r') as f:
    content = f.read()

if "import com.example.util.JalaliCalendarHelper" not in content:
    content = content.replace("import androidx.compose.runtime.Composable", "import androidx.compose.runtime.Composable\nimport com.example.util.JalaliCalendarHelper")

formatDate_target = '''    fun formatDate(millis: Long): String {
        val sdf = SimpleDateFormat("yyyy/MM/dd", Locale.US)
        return toPersianDigits(sdf.format(Date(millis)))
    }'''

formatDate_replacement = '''    fun formatDate(millis: Long): String {
        val d = JalaliCalendarHelper.getJalaliDateFromMillis(millis)
        return toPersianDigits("${d.dayOfWeekName}، ${d.day} ${d.monthName} ${d.year}")
    }'''

content = content.replace(formatDate_target, formatDate_replacement)

with open('app/src/main/java/com/example/ui/components/Formatters.kt', 'w') as f:
    f.write(content)
