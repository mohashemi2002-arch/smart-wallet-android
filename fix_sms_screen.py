import re

with open('app/src/main/java/com/example/ui/screens/PendingSmsInboxScreen.kt', 'r') as f:
    content = f.read()

target = '''                Surface(
                    color = Color(0xFF333E52),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "تراکنش بانکی",
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }'''

replacement = '''                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    com.example.util.BankLogoHelper.BankLogoView(
                        bankName = transaction.merchantName,
                        cardNumber = transaction.cardNumber,
                        size = 32.dp,
                        containerColor = Color.Transparent
                    )
                    Surface(
                        color = Color(0xFF333E52),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = transaction.merchantName.ifBlank { "تراکنش بانکی" },
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }'''

content = content.replace(target, replacement)

with open('app/src/main/java/com/example/ui/screens/PendingSmsInboxScreen.kt', 'w') as f:
    f.write(content)
