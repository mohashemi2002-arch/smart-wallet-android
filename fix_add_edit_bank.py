import re
import os

files = [
    'app/src/main/java/com/example/ui/components/AddBankAccountDialog.kt',
    'app/src/main/java/com/example/ui/components/EditAccountBottomSheet.kt'
]

target = '''                                    Surface(
                                        shape = CircleShape,
                                        color = Color(android.graphics.Color.parseColor(selectedColorHex)).copy(alpha = 0.3f),
                                        modifier = Modifier.size(20.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text("🏦", fontSize = 10.sp)
                                        }
                                    }'''

replacement = '''                                    com.example.util.BankLogoHelper.BankLogoView(
                                        bankName = bankName,
                                        cardNumber = cardNumber,
                                        size = 20.dp
                                    )'''

for f in files:
    if os.path.exists(f):
        with open(f, 'r') as file:
            content = file.read()
        
        content = content.replace(target, replacement)
        
        with open(f, 'w') as file:
            file.write(content)

