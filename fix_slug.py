import re

with open('app/src/main/java/com/example/util/BankLogoHelper.kt', 'r') as f:
    content = f.read()

target = '''        val detected = BankBinHelper.getBankInfoFromCardNumber(cardNumber ?: "")
        val name = bankName ?: detected?.name ?: ""'''

replacement = '''        val detected = BankBinHelper.getBankInfoFromCardNumber(cardNumber ?: "")
        val name = "${detected?.name ?: ""} ${bankName ?: ""}"'''

content = content.replace(target, replacement)

with open('app/src/main/java/com/example/util/BankLogoHelper.kt', 'w') as f:
    f.write(content)
