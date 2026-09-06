import re

with open('app/src/main/java/com/example/ui/screens/DashboardScreen.kt', 'r') as f:
    content = f.read()

target1 = '''            onDateSelected = { start, end, title ->
                viewModel.updateDateFilter(start, end, title)
            }'''
            
replacement1 = '''            onDateSelected = { start, end, title ->
                // Handled in dialog internally or via manual mapping if used
            }'''

target2 = '''                                            Text(
                                                text = selectedDateFilter?.displayTitle ?: "کل تراکنش‌ها",
                                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                                color = Color.White
                                            )'''

replacement2 = '''                                            Text(
                                                text = when (val filter = selectedDateFilter) {
                                                    is FinanceViewModel.DateFilter.SpecificDay -> "${filter.jDay} ${com.example.util.JalaliCalendarHelper.monthNames[filter.jMonth - 1]}"
                                                    is FinanceViewModel.DateFilter.FullMonth -> "${com.example.util.JalaliCalendarHelper.monthNames[filter.jMonth - 1]} ${filter.jYear}"
                                                    null -> "کل تراکنش‌ها"
                                                },
                                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                                color = Color.White
                                            )'''

content = content.replace(target1, replacement1)
content = content.replace(target2, replacement2)

with open('app/src/main/java/com/example/ui/screens/DashboardScreen.kt', 'w') as f:
    f.write(content)
