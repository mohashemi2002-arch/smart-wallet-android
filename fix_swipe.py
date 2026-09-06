import re
with open('app/src/main/java/com/example/ui/screens/DashboardScreen.kt', 'r') as f:
    content = f.read()

# Add haptic feedback and swipe to dismiss to RecentTransactionRow
old_func = r"""@Composable
fun RecentTransactionRow\(
    title: String,
    amountText: String,
    dateText: String,
    isIncome: Boolean,
    iconBgColor: Color,
    iconVector: androidx\.compose\.ui\.graphics\.vector\.ImageVector
\) \{
    Row\([\s\S]*?\}
\}"""

new_func = """@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun RecentTransactionRow(
    title: String,
    amountText: String,
    dateText: String,
    isIncome: Boolean,
    iconBgColor: Color,
    iconVector: androidx.compose.ui.graphics.vector.ImageVector,
    onDelete: (() -> Unit)? = null
) {
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current
    var hasVibrated by remember { mutableStateOf(false) }

    val dismissState = androidx.compose.material3.rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == androidx.compose.material3.SwipeToDismissBoxValue.EndToStart) {
                onDelete?.invoke()
                true
            } else {
                false
            }
        }
    )

    LaunchedEffect(dismissState.targetValue) {
        if (dismissState.targetValue == androidx.compose.material3.SwipeToDismissBoxValue.EndToStart && !hasVibrated) {
            haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
            hasVibrated = true
        } else if (dismissState.targetValue == androidx.compose.material3.SwipeToDismissBoxValue.Settled) {
            hasVibrated = false
        }
    }

    androidx.compose.material3.SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = false,
        enableDismissFromEndToStart = onDelete != null,
        backgroundContent = {
            val color = if (dismissState.dismissDirection == androidx.compose.material3.SwipeToDismissBoxValue.EndToStart) {
                Color(0xFFE53935)
            } else {
                Color.Transparent
            }
            
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color, RoundedCornerShape(12.dp))
                    .padding(end = 16.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                if (dismissState.dismissDirection == androidx.compose.material3.SwipeToDismissBoxValue.EndToStart) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "حذف",
                        tint = Color.White
                    )
                }
            }
        }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().background(Color(0xFF252E3E)),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = amountText,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = if (isIncome) Color(0xFF81C784) else Color(0xFFE57373)
                )
                Text(
                    text = dateText,
                    fontSize = 10.sp,
                    color = Color(0xFFB0BEC5)
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = iconBgColor,
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = iconVector,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}"""

content = re.sub(old_func, new_func, content)

# Now, we also need to pass the onDelete to the usage of RecentTransactionRow in DashboardScreen.kt
usage_old = """                                    RecentTransactionRow(
                                        title = tx.categoryName.ifBlank { "نامشخص" },
                                        amountText = "$sign ${PersianFormatters.formatMoney(tx.amountToman, currencyUnit)}",
                                        dateText = PersianFormatters.formatDate(tx.dateMillis),
                                        isIncome = isInc,
                                        iconBgColor = iconInfo.first,
                                        iconVector = iconInfo.second
                                    )"""

usage_new = """                                    RecentTransactionRow(
                                        title = tx.categoryName.ifBlank { "نامشخص" },
                                        amountText = "$sign ${PersianFormatters.formatMoney(tx.amountToman, currencyUnit)}",
                                        dateText = PersianFormatters.formatDate(tx.dateMillis),
                                        isIncome = isInc,
                                        iconBgColor = iconInfo.first,
                                        iconVector = iconInfo.second,
                                        onDelete = { viewModel.deleteTransaction(tx) }
                                    )"""

content = content.replace(usage_old, usage_new)

with open('app/src/main/java/com/example/ui/screens/DashboardScreen.kt', 'w') as f:
    f.write(content)

