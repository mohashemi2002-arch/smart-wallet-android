import re

with open('app/src/main/java/com/example/ui/components/JalaliCalendarDialog.kt', 'r') as f:
    content = f.read()

content = content.replace("TransactionTransactionDateFilter", "TransactionDateFilter")

with open('app/src/main/java/com/example/ui/components/JalaliCalendarDialog.kt', 'w') as f:
    f.write(content)
