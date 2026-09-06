import re

with open('app/src/main/java/com/example/ui/components/AllTransactionsDialog.kt', 'r') as f:
    content = f.read()

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
