import re

with open('app/src/main/java/com/example/ui/components/BankCardItem.kt', 'r') as f:
    content = f.read()

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
                    }
                    if (onDelete != null) {'''

replacement = '''                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        com.example.util.BankLogoHelper.BankLogoView(
                            bankName = account.bankName,
                            cardNumber = account.cardNumber,
                            size = 36.dp
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
                    }
                    if (onDelete != null) {'''

content = content.replace(target, replacement)

with open('app/src/main/java/com/example/ui/components/BankCardItem.kt', 'w') as f:
    f.write(content)
