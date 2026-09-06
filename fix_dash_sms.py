with open('app/src/main/java/com/example/ui/screens/DashboardScreen.kt', 'r') as f:
    content = f.read()

start_idx = content.find('            // 3. New SMS Notification Card')
end_idx = content.find('            // 5. Recent Transactions', start_idx)

replacement = """            // 3. Reactive Pending SMS Banner
            if (pendingTransactions.isNotEmpty()) {
                item {
                    PaddingBox {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onNavigateToSms() },
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF5C82E6)),
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Icon(androidx.compose.material.icons.Icons.Default.Inbox, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                                    Column {
                                        Text(
                                            text = "شما ${PersianFormatters.toPersianDigits(pendingTransactions.size.toString())} پیامک بررسی‌نشده دارید",
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = Color.White
                                        )
                                        Text(
                                            text = "(برای بررسی و ثبت لمس کنید)",
                                            fontSize = 11.sp,
                                            color = Color.White.copy(alpha = 0.8f)
                                        )
                                    }
                                }
                                Icon(androidx.compose.material.icons.Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color.White)
                            }
                        }
                    }
                }
            }

"""

if start_idx != -1 and end_idx != -1:
    content = content[:start_idx] + replacement + content[end_idx:]
    with open('app/src/main/java/com/example/ui/screens/DashboardScreen.kt', 'w') as f:
        f.write(content)
else:
    print("Could not find banner block")
