import re
import os

files_with_banklogoview = [
    'app/src/main/java/com/example/ui/components/AddBankAccountDialog.kt',
    'app/src/main/java/com/example/ui/screens/PendingSmsInboxScreen.kt'
]

for f in files_with_banklogoview:
    if os.path.exists(f):
        with open(f, 'r') as file:
            content = file.read()
        content = content.replace("com.example.util.BankLogoHelper.BankLogoView", "com.example.ui.components.BankLogoImage")
        with open(f, 'w') as file:
            file.write(content)

# Update EditAccountBottomSheet which might also have it, or might not have been updated
edit_sheet = 'app/src/main/java/com/example/ui/components/EditAccountBottomSheet.kt'
if os.path.exists(edit_sheet):
    with open(edit_sheet, 'r') as file:
        content = file.read()
    content = content.replace("com.example.util.BankLogoHelper.BankLogoView", "com.example.ui.components.BankLogoImage")
    with open(edit_sheet, 'w') as file:
        file.write(content)

# Now fix BankCardItem
bank_card = 'app/src/main/java/com/example/ui/components/BankCardItem.kt'
if os.path.exists(bank_card):
    with open(bank_card, 'r') as file:
        content = file.read()
        
    target = '''                    Column {
                        Text(
                            text = account.bankName,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                        Text(
                            text = account.accountName,
                            fontSize = 10.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }'''
                    
    replacement = '''                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        com.example.ui.components.BankLogoImage(
                            bankName = account.bankName,
                            cardNumber = account.cardNumber,
                            size = 42.dp,
                            containerColor = Color.White.copy(alpha = 0.2f)
                        )
                        Column {
                            Text(
                                text = account.bankName,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                            Text(
                                text = account.accountName,
                                fontSize = 10.sp,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }'''
    content = content.replace(target, replacement)
    
    with open(bank_card, 'w') as file:
        file.write(content)

