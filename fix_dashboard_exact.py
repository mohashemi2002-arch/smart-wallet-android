import re

with open('app/src/main/java/com/example/ui/screens/DashboardScreen.kt', 'r') as f:
    content = f.read()

target_block = '''                                    Column {
                                        Text(
                                            text = "امروز",
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = Color.White
                                        )
                                        Text(
                                            text = "یک‌شنبه، ۱۱ مرداد",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color(0xFFB0BEC5)
                                        )
                                    }'''

replacement_block = '''                                    Column(modifier = Modifier.clickable { showDatePickerDialog = true }) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = selectedDateFilter?.displayTitle ?: "کل تراکنش‌ها",
                                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                                color = Color.White
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Icon(Icons.Default.DateRange, contentDescription = "تقویم", tint = Color(0xFF7FA2FF), modifier = Modifier.size(20.dp))
                                        }
                                        Text(
                                            text = if (selectedDateFilter == null) "نمایش همه" else "تغییر تاریخ",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color(0xFFB0BEC5)
                                        )
                                    }'''

content = content.replace(target_block, replacement_block)

# Let's ensure DateRange is imported
if "import androidx.compose.material.icons.filled.DateRange" not in content:
    content = content.replace("import androidx.compose.material.icons.filled.*", "import androidx.compose.material.icons.filled.*\nimport androidx.compose.material.icons.filled.DateRange")

with open('app/src/main/java/com/example/ui/screens/DashboardScreen.kt', 'w') as f:
    f.write(content)
