import re
with open('app/src/main/java/com/example/ui/screens/DashboardScreen.kt', 'r') as f:
    content = f.read()

# Replace the hardcoded "1 پیامک جدید!" logic
start_idx = content.find('            // 3. New SMS Notification Card ("۱ پیامک جدید!")')
end_idx = content.find('            // 4. Accounts Slider', start_idx)

replacement = """
            if (pendingTransactions.isNotEmpty()) {
                item {
                    PaddingBox {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedPendingTx = pendingTransactions.first() },
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF252E3E)),
                            shape = RoundedCornerShape(24.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF333E52))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = Color(0xFF333E52),
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(Icons.Default.Refresh, contentDescription = "پیامک جدید", tint = Color.White, modifier = Modifier.size(18.dp))
                                        }
                                    }

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            text = "${PersianFormatters.toPersianDigits(pendingTransactions.size.toString())} پیامک در انتظار تایید",
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = Color.White
                                        )
                                        Icon(Icons.Default.MoreHoriz, contentDescription = null, tint = Color.White)
                                    }
                                }

                                HorizontalDivider(color = Color(0xFF333E52))

                                val firstTx = pendingTransactions.first()
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconButton(onClick = { viewModel.rejectPendingTransaction(firstTx) }) {
                                        Icon(Icons.Default.DeleteOutline, contentDescription = "حذف", tint = Color(0xFFB0BEC5))
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = PersianFormatters.formatMoney(firstTx.amountToman, currencyUnit),
                                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                            color = if (firstTx.type == com.example.data.model.TransactionType.INCOME) Color(0xFF81C784) else Color(0xFFE57373)
                                        )
                                        Spacer(Modifier.height(2.dp))
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(firstTx.bankName, fontSize = 11.sp, color = Color(0xFFB0BEC5))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
"""

if start_idx != -1 and end_idx != -1:
    content = content[:start_idx] + replacement + content[end_idx:]

# Ensure PendingReviewBottomSheet is shown when selectedPendingTx is not null
bottom_sheet_code = """
    if (selectedPendingTx != null) {
        com.example.ui.components.PendingReviewBottomSheet(
            transaction = selectedPendingTx!!,
            onDismiss = { selectedPendingTx = null },
            onConfirm = { updatedTx ->
                viewModel.approvePendingTransaction(updatedTx)
                selectedPendingTx = null
            },
            onIgnore = { tx ->
                viewModel.rejectPendingTransaction(tx)
                selectedPendingTx = null
            }
        )
    }
"""
if "com.example.ui.components.PendingReviewBottomSheet" not in content:
    content = content.replace("    if (showAddAccountDialog) {", bottom_sheet_code + "\n    if (showAddAccountDialog) {")

with open('app/src/main/java/com/example/ui/screens/DashboardScreen.kt', 'w') as f:
    f.write(content)
