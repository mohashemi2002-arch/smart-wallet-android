import re

with open('app/src/main/java/com/example/ui/screens/DashboardScreen.kt', 'r') as f:
    content = f.read()

target = r'''            com\.example\.ui\.components\.BankLogoImage\(
                bankName = bankName,
                cardNumber = cardNumber,
                size = 36\.dp,
                containerColor = Color\.White\.copy\(alpha = 0\.2f\)
            \)'''

replacement = r'''            com.example.ui.components.BankLogoImage(
                bankName = bankName,
                cardNumber = cardNumber,
                containerSize = 44.dp,
                logoSize = 36.dp,
                containerColor = Color.White.copy(alpha = 0.2f)
            )'''

content = re.sub(target, replacement, content)

with open('app/src/main/java/com/example/ui/screens/DashboardScreen.kt', 'w') as f:
    f.write(content)

