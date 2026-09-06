with open('app/src/main/java/com/example/ui/components/AddBankAccountDialog.kt', 'r') as f:
    content = f.read()

import re

# Update AddBankAccountDialog.kt
content = content.replace("private fun detectIranianBank", "// private fun detectIranianBank")
content = re.sub(r'detectIranianBank\(cardNumber\)', 'com.example.utils.BankBinHelper.getBankInfoFromCardNumber(cardNumber)?.let { Pair(it.name, it.colorHex) } ?: Pair("", "#1E88E5")', content)

with open('app/src/main/java/com/example/ui/components/AddBankAccountDialog.kt', 'w') as f:
    f.write(content)

# Update EditAccountBottomSheet.kt
with open('app/src/main/java/com/example/ui/components/EditAccountBottomSheet.kt', 'r') as f:
    content2 = f.read()

content2 = content2.replace("private fun detectIranianBank", "// private fun detectIranianBank")
content2 = re.sub(r'detectIranianBank\(cardNumber\)', 'com.example.utils.BankBinHelper.getBankInfoFromCardNumber(cardNumber)?.let { Pair(it.name, it.colorHex) } ?: Pair("", "#1E88E5")', content2)

with open('app/src/main/java/com/example/ui/components/EditAccountBottomSheet.kt', 'w') as f:
    f.write(content2)

