import re

with open('app/src/main/java/com/example/ui/screens/DashboardScreen.kt', 'r') as f:
    content = f.read()

# Replace the BankCardHeaderPill hardcoded list with just dynamic items + add clickable

new_cards = """
                            items(bankAccounts) { acc ->
                                BankCardHeaderPill(
                                    bankName = acc.bankName,
                                    balanceText = PersianFormatters.formatMoney(acc.balanceToman, currencyUnit),
                                    bgColor = Color(0xFF2C384A),
                                    iconColor = Color(0xFF7FA2FF),
                                    onClick = { selectedBankAccount = acc; showEditAccountDialog = true }
                                )
                            }
                            if (bankAccounts.isEmpty()) {
                                item {
                                    Text("هنوز کارتی ثبت نشده است", color = Color.Gray, fontSize = 12.sp, modifier = Modifier.padding(16.dp))
                                }
                            }"""

# Use regex to replace the hardcoded items.
content = re.sub(r'// Default Card 1: بانک ملی شعبه.*?(?=// Dynamic User Accounts)', '', content, flags=re.DOTALL)
content = re.sub(r'items\(bankAccounts\).*?iconColor = Color\(0xFF7FA2FF\)\n\s*\)\n\s*\}', new_cards, content, flags=re.DOTALL)

with open('app/src/main/java/com/example/ui/screens/DashboardScreen.kt', 'w') as f:
    f.write(content)
