with open('app/src/main/java/com/example/ui/viewmodel/FinanceViewModel.kt', 'r') as f:
    content = f.read()

# Add AppDatabase import
if 'import com.example.data.database.AppDatabase' not in content:
    content = content.replace('import com.example.data.model.*', 'import com.example.data.model.*\nimport com.example.data.database.AppDatabase')

# Fix formatTimestampToPersian
content = content.replace('PersianFormatters.formatTimestampToPersian', 'PersianFormatters.formatDateTime')

# Fix maxOfOrNull
content = content.replace('.maxOfOrNull { it.lastUpdatedTimestamp }', '.map { it.lastUpdatedTimestamp }.maxOrNull()')

with open('app/src/main/java/com/example/ui/viewmodel/FinanceViewModel.kt', 'w') as f:
    f.write(content)
