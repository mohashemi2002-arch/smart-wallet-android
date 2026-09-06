import re

with open('app/src/main/java/com/example/ui/components/JalaliCalendarDialog.kt', 'r') as f:
    content = f.read()

content = content.replace("FinanceViewModel.DateFilter", "FinanceViewModel.TransactionDateFilter")
content = content.replace("DateFilter.SpecificDay", "TransactionDateFilter.DayFilter")
content = content.replace("DateFilter.FullMonth", "TransactionDateFilter.MonthFilter")
content = content.replace("DateFilter", "TransactionDateFilter")

with open('app/src/main/java/com/example/ui/components/JalaliCalendarDialog.kt', 'w') as f:
    f.write(content)
