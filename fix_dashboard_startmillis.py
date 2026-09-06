import re

with open('app/src/main/java/com/example/ui/screens/DashboardScreen.kt', 'r') as f:
    content = f.read()

target = '''            initialMillis = selectedDateFilter?.startMillis,'''

replacement = '''            initialMillis = when(val filter = selectedDateFilter) {
                is FinanceViewModel.DateFilter.SpecificDay -> filter.startMillis
                is FinanceViewModel.DateFilter.FullMonth -> filter.startMillis
                null -> null
            },'''

content = content.replace(target, replacement)

with open('app/src/main/java/com/example/ui/screens/DashboardScreen.kt', 'w') as f:
    f.write(content)
