import re

with open('app/src/main/java/com/example/ui/screens/DashboardScreen.kt', 'r') as f:
    content = f.read()

# Replace definition
target_def = r'''fun BankCardHeaderPill\(.*?onClick: \(\) -> Unit = \{\}\n\) \{.*?Icon\(.*?imageVector = Icons\.Default\.AccountBalance,.*?modifier = Modifier\.size\(20\.dp\)\n\s*\)\n\s*\}\n\s*\}\n\s*\}\n\s*\}\n\}'''
replacement_def = '''fun BankCardHeaderPill(
    bankName: String,
    cardNumber: String,
    balanceText: String,
    bgColor: Color,
    iconColor: Color,
    onClick: () -> Unit = {}
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = bgColor,
        modifier = Modifier.height(72.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = bankName,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = balanceText,
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            com.example.ui.components.BankLogoImage(
                bankName = bankName,
                cardNumber = cardNumber,
                size = 36.dp,
                containerColor = Color.White.copy(alpha = 0.2f)
            )
        }
    }
}'''

content = re.sub(target_def, replacement_def, content, flags=re.DOTALL)

# Replace usage
target_usage = r'''BankCardHeaderPill\(\s*bankName = acc\.bankName,\s*balanceText = PersianFormatters\.formatMoney\(acc\.balanceToman, currencyUnit\),\s*bgColor = Color\(0xFF2C384A\),\s*iconColor = Color\(0xFF7FA2FF\),\s*onClick = \{ selectedBankAccount = acc; showEditAccountDialog = true \}\s*\)'''
replacement_usage = '''BankCardHeaderPill(
                                    bankName = acc.bankName,
                                    cardNumber = acc.cardNumber,
                                    balanceText = PersianFormatters.formatMoney(acc.balanceToman, currencyUnit),
                                    bgColor = Color(0xFF2C384A),
                                    iconColor = Color(0xFF7FA2FF),
                                    onClick = { selectedBankAccount = acc; showEditAccountDialog = true }
                                )'''

content = re.sub(target_usage, replacement_usage, content)

with open('app/src/main/java/com/example/ui/screens/DashboardScreen.kt', 'w') as f:
    f.write(content)

