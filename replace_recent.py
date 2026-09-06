import re
with open('app/src/main/java/com/example/ui/screens/DashboardScreen.kt', 'r') as f:
    content = f.read()

start_idx = content.find('                            // Recent Items matching Screenshot 1 & 4')
end_idx = content.find('                        }\n                    }\n                }\n            }\n        }\n    }')

replacement = """                            // Dynamic Recent Transactions
                            if (transactions.isEmpty()) {
                                Text(
                                    text = "هنوز تراکنشی ثبت نشده است.",
                                    color = Color.Gray,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(top = 16.dp, bottom = 16.dp).align(Alignment.CenterHorizontally)
                                )
                            } else {
                                transactions.take(3).forEachIndexed { index, tx ->
                                    val isInc = tx.type == com.example.data.model.TransactionType.INCOME
                                    val sign = if (isInc) "+" else "-"
                                    
                                    val iconInfo = when (tx.categoryName) {
                                        "خوراکی و سوپرمارکت" -> Color(0xFFFF7043) to Icons.Default.Fastfood
                                        "حمل و نقل" -> Color(0xFFE53935) to Icons.Default.DirectionsCar
                                        "سلامت و درمان" -> Color(0xFFE91E63) to Icons.Default.LocalHospital
                                        "پوشاک و خرید" -> Color(0xFFF57C00) to Icons.Default.ShoppingBag
                                        "درآمد و حقوق" -> Color(0xFF81C784) to Icons.Default.Payments
                                        else -> Color(0xFF607D8B) to Icons.Default.Category
                                    }
                                    
                                    RecentTransactionRow(
                                        title = tx.categoryName.ifBlank { "نامشخص" },
                                        amountText = "$sign ${PersianFormatters.formatMoney(tx.amountToman, currencyUnit)}",
                                        dateText = PersianFormatters.formatDate(tx.dateMillis),
                                        isIncome = isInc,
                                        iconBgColor = iconInfo.first,
                                        iconVector = iconInfo.second
                                    )
                                    
                                    if (index < transactions.take(3).size - 1) {
                                        HorizontalDivider(color = Color(0xFF333E52).copy(alpha = 0.5f), modifier = Modifier.padding(vertical = 8.dp))
                                    }
                                }
                            }
"""

if start_idx != -1 and end_idx != -1:
    content = content[:start_idx] + replacement + content[end_idx:]
    with open('app/src/main/java/com/example/ui/screens/DashboardScreen.kt', 'w') as f:
        f.write(content)
else:
    print("Could not find boundaries")
