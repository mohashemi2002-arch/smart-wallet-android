with open('app/src/main/java/com/example/ui/screens/DashboardScreen.kt', 'r') as f:
    content = f.read()

target = r'''            // 3. Reactive Pending SMS Banner
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
            }'''

replacement = r'''            // 3. Permanent SMS Management Banner
            item {
                PaddingBox {
                    val hasPending = pendingTransactions.isNotEmpty()
                    val containerColor = if (hasPending) Color(0xFF5C82E6) else Color(0xFF252E3E)
                    val contentColor = if (hasPending) Color.White else Color.LightGray
                    
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToSms() },
                        colors = CardDefaults.cardColors(containerColor = containerColor),
                        shape = RoundedCornerShape(24.dp),
                        border = if (!hasPending) androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF333E52)) else null
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
                                Icon(
                                    if (hasPending) androidx.compose.material.icons.Icons.Default.Inbox else androidx.compose.material.icons.Icons.Default.History, 
                                    contentDescription = null, 
                                    tint = contentColor, 
                                    modifier = Modifier.size(24.dp)
                                )
                                Column {
                                    Text(
                                        text = if (hasPending) "شما ${PersianFormatters.toPersianDigits(pendingTransactions.size.toString())} پیامک بررسی‌نشده دارید" else "تاریخچه و صندوق پیامک‌های بانکی",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = contentColor
                                    )
                                    Text(
                                        text = if (hasPending) "(برای بررسی و ثبت لمس کنید)" else "(مدیریت و افزودن دستی پیامک‌ها)",
                                        fontSize = 11.sp,
                                        color = if (hasPending) Color.White.copy(alpha = 0.8f) else Color.Gray
                                    )
                                }
                            }
                            Icon(androidx.compose.material.icons.Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = contentColor)
                        }
                    }
                }
            }'''

content = content.replace(target, replacement)

with open('app/src/main/java/com/example/ui/screens/DashboardScreen.kt', 'w') as f:
    f.write(content)
