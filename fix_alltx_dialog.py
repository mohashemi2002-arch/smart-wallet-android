import re

with open('app/src/main/java/com/example/ui/components/AllTransactionsDialog.kt', 'r') as f:
    content = f.read()

# Make sure DateFilter is imported
if "import com.example.ui.viewmodel.FinanceViewModel.DateFilter" not in content:
    content = content.replace("import com.example.util.JalaliCalendarHelper", "import com.example.util.JalaliCalendarHelper\nimport com.example.ui.viewmodel.FinanceViewModel.DateFilter")

target_sig = '''fun AllTransactionsDialog(
    transactions: List<Transaction>,
    currencyUnit: CurrencyUnit,
    onDismiss: () -> Unit,
    onDeleteTransaction: ((Transaction) -> Unit)? = null
) {'''

replacement_sig = '''fun AllTransactionsDialog(
    transactions: List<Transaction>,
    currencyUnit: CurrencyUnit,
    selectedFilter: DateFilter?,
    onFilterSelected: (DateFilter?) -> Unit,
    onDismiss: () -> Unit,
    onDeleteTransaction: ((Transaction) -> Unit)? = null
) {
    var showCalendarDialog by remember { mutableStateOf(false) }

    if (showCalendarDialog) {
        JalaliCalendarDialog(
            initialFilter = selectedFilter,
            transactions = transactions, // Or perhaps pass all transactions if we want dots, but here we only have filtered ones. Wait, if we want dots for all, we need all tx. We'll just pass transactions.
            onFilterSelected = { filter -> onFilterSelected(filter) },
            onDismiss = { showCalendarDialog = false }
        )
    }'''

content = content.replace(target_sig, replacement_sig)

# Now, we replace the Header Row containing the arrows and text.
# The user wants:
# Left/Right arrows (< >) should jump month/day. 
# And clicking the text opens the dialog.

target_header = '''                // Header
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "همه تراکنش‌ها",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color(0xFF333E52)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "قبلی",
                                tint = Color(0xFFCAC4D0),
                                modifier = Modifier.size(16.dp)
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "همه تراکنش‌ها",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                            }
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "بعدی",
                                tint = Color(0xFFCAC4D0),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }'''

replacement_header = '''                // Header
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "تراکنش‌ها",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    
                    val displayTitle = when (val filter = selectedFilter) {
                        is DateFilter.SpecificDay -> {
                            val currentJalali = JalaliCalendarHelper.getJalaliDateFromMillis(System.currentTimeMillis())
                            if (filter.jYear == currentJalali.year && filter.jMonth == currentJalali.month && filter.jDay == currentJalali.day) {
                                "امروز - ${filter.jDay} ${JalaliCalendarHelper.monthNames[filter.jMonth - 1]}"
                            } else {
                                "${filter.jDay} ${JalaliCalendarHelper.monthNames[filter.jMonth - 1]} ${filter.jYear}"
                            }
                        }
                        is DateFilter.FullMonth -> "${JalaliCalendarHelper.monthNames[filter.jMonth - 1]} ${filter.jYear}"
                        null -> "کل تراکنش‌ها"
                    }

                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color(0xFF333E52)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            IconButton(
                                onClick = {
                                    // Jump Previous
                                    when (val filter = selectedFilter) {
                                        is DateFilter.SpecificDay -> {
                                            val p = filter.startMillis - 86400000L
                                            val jd = JalaliCalendarHelper.getJalaliDateFromMillis(p)
                                            val (s, e) = JalaliCalendarHelper.getStartAndEndOfJalaliDay(jd.year, jd.month, jd.day)
                                            onFilterSelected(DateFilter.SpecificDay(jd.year, jd.month, jd.day, s, e))
                                        }
                                        is DateFilter.FullMonth -> {
                                            var m = filter.jMonth - 1
                                            var y = filter.jYear
                                            if (m == 0) { m = 12; y-- }
                                            val (s, e) = JalaliCalendarHelper.getStartAndEndOfJalaliMonth(y, m)
                                            onFilterSelected(DateFilter.FullMonth(y, m, s, e))
                                        }
                                        null -> {}
                                    }
                                },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "قبلی",
                                    tint = Color(0xFFCAC4D0),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            
                            Row(
                                verticalAlignment = Alignment.CenterVertically, 
                                modifier = Modifier.clickable { showCalendarDialog = true }.padding(horizontal = 4.dp)
                            ) {
                                Text(
                                    text = displayTitle,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Spacer(Modifier.width(4.dp))
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                            }
                            
                            IconButton(
                                onClick = {
                                    // Jump Next
                                    when (val filter = selectedFilter) {
                                        is DateFilter.SpecificDay -> {
                                            val n = filter.startMillis + 86400000L
                                            val jd = JalaliCalendarHelper.getJalaliDateFromMillis(n)
                                            val (s, e) = JalaliCalendarHelper.getStartAndEndOfJalaliDay(jd.year, jd.month, jd.day)
                                            onFilterSelected(DateFilter.SpecificDay(jd.year, jd.month, jd.day, s, e))
                                        }
                                        is DateFilter.FullMonth -> {
                                            var m = filter.jMonth + 1
                                            var y = filter.jYear
                                            if (m == 13) { m = 1; y++ }
                                            val (s, e) = JalaliCalendarHelper.getStartAndEndOfJalaliMonth(y, m)
                                            onFilterSelected(DateFilter.FullMonth(y, m, s, e))
                                        }
                                        null -> {}
                                    }
                                },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = "بعدی",
                                    tint = Color(0xFFCAC4D0),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }'''

content = content.replace(target_header, replacement_header)

with open('app/src/main/java/com/example/ui/components/AllTransactionsDialog.kt', 'w') as f:
    f.write(content)
