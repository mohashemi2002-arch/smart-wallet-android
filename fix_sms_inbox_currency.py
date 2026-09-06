import re
with open('app/src/main/java/com/example/ui/screens/PendingSmsInboxScreen.kt', 'r') as f:
    content = f.read()

content = content.replace(
    'text = PersianFormatters.formatMoney(transaction.amountToman, com.example.data.model.CurrencyUnit.TOMAN),',
    'text = PersianFormatters.formatMoney(transaction.amountToman, com.example.ui.components.CurrencyUnit.TOMAN),'
)

with open('app/src/main/java/com/example/ui/screens/PendingSmsInboxScreen.kt', 'w') as f:
    f.write(content)
