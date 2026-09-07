import re

with open('app/src/main/java/com/example/ui/components/AddBankAccountDialog.kt', 'r') as f:
    content = f.read()

target = r'''                                    com\.example\.ui\.components\.BankLogoImage\(
                                        bankName = bankName,
                                        cardNumber = cardNumber,
                                        size = 20\.dp
                                    \)'''

replacement = r'''                                    com.example.ui.components.BankLogoImage(
                                        bankName = bankName,
                                        cardNumber = cardNumber,
                                        containerSize = 56.dp,
                                        logoSize = 48.dp,
                                        containerColor = Color.White.copy(alpha = 0.2f)
                                    )'''

content = re.sub(target, replacement, content)

with open('app/src/main/java/com/example/ui/components/AddBankAccountDialog.kt', 'w') as f:
    f.write(content)

