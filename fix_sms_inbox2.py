import re
with open('app/src/main/java/com/example/ui/screens/PendingSmsInboxScreen.kt', 'r') as f:
    content = f.read()

content = content.replace(
    'text = transaction.bankName,',
    'text = "تراکنش بانکی",'
)

with open('app/src/main/java/com/example/ui/screens/PendingSmsInboxScreen.kt', 'w') as f:
    f.write(content)
