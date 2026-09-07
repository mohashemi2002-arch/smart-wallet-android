import re

with open('app/src/main/java/com/example/ui/screens/DashboardScreen.kt', 'r') as f:
    content = f.read()

# Replace formatMoney usages
content = re.sub(r'PersianFormatters\.formatMoney\(\s*netWorth,\s*currencyUnit\s*\)', r'netWorth.formatAsToman() + if (currencyUnit == CurrencyUnit.RIAL) " ریال" else " تومان"', content)

# Dashboard income/expense
content = re.sub(r'PersianFormatters\.formatMoney\(\s*monthlyIncome,\s*currencyUnit\s*\)', r'monthlyIncome.formatAsToman() + if (currencyUnit == CurrencyUnit.RIAL) " ریال" else " تومان"', content)
content = re.sub(r'PersianFormatters\.formatMoney\(\s*monthlyExpense,\s*currencyUnit\s*\)', r'monthlyExpense.formatAsToman() + if (currencyUnit == CurrencyUnit.RIAL) " ریال" else " تومان"', content)

# Bank card in Dashboard
content = re.sub(r'balanceText = PersianFormatters\.formatMoney\(acc\.balanceToman,\s*currencyUnit\)', r'balanceText = acc.balanceToman.formatAsToman() + if (currencyUnit == CurrencyUnit.RIAL) " ریال" else " تومان"', content)

# Recent transactions
content = re.sub(r'amountText = "\$sign \$\{PersianFormatters\.formatMoney\(tx\.amountToman,\s*currencyUnit\)\}"', r'amountText = "$sign ${tx.amountToman.formatAsToman()} ${if (currencyUnit == CurrencyUnit.RIAL) "ریال" else "تومان"}"', content)

# Also market rates
content = re.sub(r'PersianFormatters\.formatMoney\(it,\s*currencyUnit\)', r'it.formatAsToman() + if (currencyUnit == CurrencyUnit.RIAL) " ریال" else " تومان"', content)


# And in AddTransactionDialog
target_add_tx = r'''var amountText by remember \{ mutableStateOf\(""\) \}

    AlertDialog\('''

replacement_add_tx = r'''var amountText by remember { mutableStateOf("") }

    AlertDialog('''

# Let's check how AddTransactionDialog is structured first.

with open('app/src/main/java/com/example/ui/screens/DashboardScreen.kt', 'w') as f:
    f.write(content)

