import re

with open('app/src/main/java/com/example/ui/components/JalaliCalendarDialog.kt', 'r') as f:
    content = f.read()

# Fix creation of DayFilter and MonthFilter
content = re.sub(r'TransactionDateFilter\.DayFilter\(([^,]+),([^,]+),([^,]+),[^,]+,[^)]+\)', r'TransactionDateFilter.DayFilter(\1, \2, \3)', content)
content = re.sub(r'TransactionDateFilter\.MonthFilter\(([^,]+),([^,]+),[^,]+,[^)]+\)', r'TransactionDateFilter.MonthFilter(\1, \2)', content)

# Check missing import TransactionDateFilter
if "import com.example.ui.viewmodel.FinanceViewModel.TransactionDateFilter" not in content:
    content = content.replace("import com.example.util.JalaliCalendarHelper", "import com.example.util.JalaliCalendarHelper\nimport com.example.ui.viewmodel.FinanceViewModel.TransactionDateFilter")

with open('app/src/main/java/com/example/ui/components/JalaliCalendarDialog.kt', 'w') as f:
    f.write(content)

