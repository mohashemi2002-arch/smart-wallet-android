package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.BankAccount
import com.example.data.model.Transaction
import com.example.data.model.TransactionType
import com.example.data.repository.CategoryRepository
import com.example.util.JalaliCalendarHelper

enum class TransactionMode(val title: String) {
    EXPENSE("خرج"),
    INCOME("دخل"),
    TRANSFER("جابجایی")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddOrEditTransactionDialog(
    transactionToEdit: Transaction? = null,
    bankAccounts: List<BankAccount>,
    currencyUnit: CurrencyUnit,
    onDismiss: () -> Unit,
    onDelete: ((Transaction) -> Unit)? = null,
    onConfirm: (
        amountToman: Long,
        type: TransactionType,
        category: String,
        merchantOrNotes: String,
        sourceAccount: BankAccount?,
        destinationAccount: BankAccount?,
        dateMillis: Long
    ) -> Unit
) {
    val context = LocalContext.current
    val categoryRepo = remember { CategoryRepository(context) }

    // Initial Mode
    val initialMode = remember(transactionToEdit) {
        when {
            transactionToEdit == null -> TransactionMode.EXPENSE
            transactionToEdit.categoryName == "جابجایی" || transactionToEdit.notes?.contains("جابجایی") == true -> TransactionMode.TRANSFER
            transactionToEdit.type == TransactionType.INCOME -> TransactionMode.INCOME
            else -> TransactionMode.EXPENSE
        }
    }

    var selectedMode by remember { mutableStateOf(initialMode) }
    var amountDigits by remember {
        mutableStateOf(transactionToEdit?.let { (if (currencyUnit == CurrencyUnit.RIAL) it.amountToman * 10 else it.amountToman).toString() } ?: "")
    }
    var notesText by remember { mutableStateOf(transactionToEdit?.notes ?: transactionToEdit?.merchantName ?: "") }

    // Accounts
    var sourceAccount by remember {
        mutableStateOf(
            bankAccounts.firstOrNull { it.cardNumber == transactionToEdit?.cardNumber }
                ?: bankAccounts.firstOrNull()
        )
    }
    var destinationAccount by remember {
        mutableStateOf(
            bankAccounts.firstOrNull { it.id != sourceAccount?.id }
                ?: bankAccounts.firstOrNull()
        )
    }

    // Category
    var selectedCategoryName by remember {
        mutableStateOf(transactionToEdit?.categoryName ?: if (selectedMode == TransactionMode.INCOME) "حقوق" else "نقدی")
    }

    // Date
    var currentDateMillis by remember { mutableStateOf(transactionToEdit?.dateMillis ?: System.currentTimeMillis()) }
    var showDatePickerDialog by remember { mutableStateOf(false) }

    // Pickers
    var showCategoryPickerSheet by remember { mutableStateOf(false) }
    var pickingAccountForSource by remember { mutableStateOf<Boolean?>(null) } // true: source, false: dest, null: hidden
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val formattedDate = remember(currentDateMillis) {
        val d = JalaliCalendarHelper.getJalaliDateFromMillis(currentDateMillis)
        PersianFormatters.toPersianDigits("${d.dayOfWeekName}، ${d.day} ${d.monthName} ${d.year}")
    }

    val currencyLabel = if (currencyUnit == CurrencyUnit.RIAL) "ریال" else "تومان"

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color(0xFF0F172A)
        ) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            Text(
                                text = if (transactionToEdit != null) "ویرایش تراکنش" else "ثبت تراکنش",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                ),
                                color = Color.White
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = onDismiss) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "بازگشت",
                                    tint = Color.White
                                )
                            }
                        },
                        actions = {
                            if (transactionToEdit != null && onDelete != null) {
                                IconButton(onClick = {
                                    onDelete(transactionToEdit)
                                    onDismiss()
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "حذف تراکنش",
                                        tint = Color(0xFFF87171)
                                    )
                                }
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0F172A))
                    )
                },
                containerColor = Color(0xFF0F172A)
            ) { padding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(horizontal = 16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Spacer(modifier = Modifier.height(4.dp))

                    // 1. Segmented Control: خرج | دخل | جابجایی
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFF1E293B)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            TransactionMode.entries.forEach { mode ->
                                val isSelected = selectedMode == mode
                                val activeBgColor = when (mode) {
                                    TransactionMode.TRANSFER -> Color(0xFF059669)
                                    TransactionMode.INCOME -> Color(0xFF10B981)
                                    TransactionMode.EXPENSE -> Color(0xFF2563EB)
                                }

                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(44.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable {
                                            selectedMode = mode
                                            if (mode == TransactionMode.EXPENSE && (selectedCategoryName == "حقوق" || selectedCategoryName == "جابجایی")) {
                                                selectedCategoryName = "خوردنی"
                                            } else if (mode == TransactionMode.INCOME && (selectedCategoryName == "خوردنی" || selectedCategoryName == "جابجایی")) {
                                                selectedCategoryName = "حقوق"
                                            } else if (mode == TransactionMode.TRANSFER) {
                                                selectedCategoryName = "جابجایی"
                                            }
                                        },
                                    color = if (isSelected) activeBgColor.copy(alpha = 0.25f) else Color.Transparent,
                                    border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, activeBgColor) else null,
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = mode.title,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            fontSize = 15.sp,
                                            color = if (isSelected) Color.White else Color(0xFF94A3B8)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 2. Amount Input Card
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        color = Color(0xFF1E293B)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = currencyLabel,
                                color = Color(0xFF94A3B8),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )

                            OutlinedTextField(
                                value = amountDigits,
                                onValueChange = { input ->
                                    val digitsOnly = PersianFormatters.toEnglishDigits(input).filter { it.isDigit() }
                                    amountDigits = digitsOnly
                                    errorMessage = null
                                },
                                visualTransformation = ThousandsSeparatorVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                textStyle = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 22.sp,
                                    color = Color.White
                                ),
                                placeholder = {
                                    Text("۰", fontSize = 22.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Bold)
                                },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color.Transparent,
                                    unfocusedBorderColor = Color.Transparent,
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent
                                ),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // 3. Mode Dependent Section (Transfer vs Expense/Income)
                    if (selectedMode == TransactionMode.TRANSFER) {
                        // Transfer View: Side-by-side Source (مبدا) and Destination (مقصد) cards with arrow in between
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Source Account Card (مبدا)
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(18.dp))
                                    .clickable { pickingAccountForSource = true },
                                color = Color(0xFF1E293B),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
                            ) {
                                Column(
                                    modifier = Modifier.padding(14.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "مبدا",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = Color(0xFF94A3B8)
                                    )
                                    BankLogoImage(
                                        bankName = sourceAccount?.bankName,
                                        cardNumber = sourceAccount?.cardNumber,
                                        containerSize = 44.dp,
                                        logoSize = 36.dp
                                    )
                                    Text(
                                        text = sourceAccount?.bankName ?: "انتخاب کارت",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = Color.White,
                                        maxLines = 1
                                    )
                                }
                            }

                            // Left Arrow between cards (pointing from Source to Destination)
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF27354A)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "انتقال",
                                    tint = Color(0xFF38BDF8),
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            // Destination Account Card (مقصد)
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(18.dp))
                                    .clickable { pickingAccountForSource = false },
                                color = Color(0xFF1E293B),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
                            ) {
                                Column(
                                    modifier = Modifier.padding(14.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "مقصد",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = Color(0xFF94A3B8)
                                    )
                                    BankLogoImage(
                                        bankName = destinationAccount?.bankName,
                                        cardNumber = destinationAccount?.cardNumber,
                                        containerSize = 44.dp,
                                        logoSize = 36.dp
                                    )
                                    Text(
                                        text = destinationAccount?.bankName ?: "انتخاب کارت",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = Color.White,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    } else {
                        // Expense or Income: Category Card + Account Card
                        // Category Selector Card
                        val catInfo = CategoryIconMap.getIconForCategory(selectedCategoryName)
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(18.dp))
                                .clickable { showCategoryPickerSheet = true },
                            color = Color(0xFF1E293B),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(catInfo.first.copy(alpha = 0.2f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = catInfo.second,
                                            contentDescription = selectedCategoryName,
                                            tint = catInfo.first,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                    Column {
                                        Text("دسته‌بندی", style = MaterialTheme.typography.labelSmall, color = Color(0xFF94A3B8))
                                        Text(selectedCategoryName, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = Color.White)
                                    }
                                }

                                Icon(
                                    imageVector = Icons.Default.ChevronLeft,
                                    contentDescription = "انتخاب دسته‌بندی",
                                    tint = Color(0xFF94A3B8)
                                )
                            }
                        }

                        // Account Selector Card
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(18.dp))
                                .clickable { pickingAccountForSource = true },
                            color = Color(0xFF1E293B),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    BankLogoImage(
                                        bankName = sourceAccount?.bankName,
                                        cardNumber = sourceAccount?.cardNumber,
                                        containerSize = 40.dp,
                                        logoSize = 32.dp
                                    )
                                    Column {
                                        Text("حساب بانکی", style = MaterialTheme.typography.labelSmall, color = Color(0xFF94A3B8))
                                        Text(sourceAccount?.bankName ?: "انتخاب کارت", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = Color.White)
                                    }
                                }

                                Icon(
                                    imageVector = Icons.Default.ChevronLeft,
                                    contentDescription = "تغییر کارت",
                                    tint = Color(0xFF94A3B8)
                                )
                            }
                        }
                    }

                    // 4. Date Row with < > Controls
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        color = Color(0xFF1E293B)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            IconButton(
                                onClick = { currentDateMillis -= 24 * 60 * 60 * 1000L },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "روز قبل",
                                    tint = Color(0xFF94A3B8),
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.clickable { showDatePickerDialog = true }
                            ) {
                                Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(20.dp))
                                Text(
                                    text = formattedDate,
                                    color = Color.White,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp
                                )
                            }

                            IconButton(
                                onClick = { currentDateMillis += 24 * 60 * 60 * 1000L },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = "روز بعد",
                                    tint = Color(0xFF94A3B8),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    // 5. Notes / Description
                    OutlinedTextField(
                        value = notesText,
                        onValueChange = { notesText = it },
                        label = { Text("توضیحات", color = Color(0xFF94A3B8)) },
                        placeholder = { Text("توضیحات یا طرف حساب...", color = Color(0xFF64748B)) },
                        leadingIcon = {
                            Icon(Icons.Default.Notes, contentDescription = null, tint = Color(0xFF94A3B8))
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF38BDF8),
                            unfocusedBorderColor = Color(0xFF334155),
                            focusedContainerColor = Color(0xFF1E293B),
                            unfocusedContainerColor = Color(0xFF1E293B)
                        )
                    )

                    if (errorMessage != null) {
                        Text(
                            text = errorMessage ?: "",
                            color = Color(0xFFEF4444),
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 6. Confirm / Submit Button
                    Button(
                        onClick = {
                            val rawAmount = amountDigits.toLongOrNull() ?: 0L
                            if (rawAmount <= 0L) {
                                errorMessage = "لطفاً مبلغ معتبری وارد کنید"
                                return@Button
                            }

                            val finalAmountToman = if (currencyUnit == CurrencyUnit.RIAL) {
                                rawAmount / 10L
                            } else {
                                rawAmount
                            }

                            if (selectedMode == TransactionMode.TRANSFER) {
                                if (sourceAccount == null || destinationAccount == null) {
                                    errorMessage = "لطفاً حساب مبدا و مقصد را مشخص کنید"
                                    return@Button
                                }
                                if (sourceAccount?.id == destinationAccount?.id) {
                                    errorMessage = "حساب مبدا و مقصد نمی‌توانند یکسان باشند"
                                    return@Button
                                }
                            }

                            val txType = when (selectedMode) {
                                TransactionMode.INCOME -> TransactionType.INCOME
                                TransactionMode.EXPENSE -> TransactionType.EXPENSE
                                TransactionMode.TRANSFER -> TransactionType.EXPENSE // standard representation in finance engine
                            }

                            val finalCat = if (selectedMode == TransactionMode.TRANSFER) "جابجایی" else selectedCategoryName
                            val finalMerchant = if (selectedMode == TransactionMode.TRANSFER) {
                                "انتقال از ${sourceAccount?.bankName} به ${destinationAccount?.bankName}"
                            } else {
                                notesText.ifBlank { "تراکنش دستی" }
                            }

                            onConfirm(
                                finalAmountToman,
                                txType,
                                finalCat,
                                finalMerchant,
                                sourceAccount,
                                destinationAccount,
                                currentDateMillis
                            )
                            onDismiss()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (transactionToEdit != null) "ویرایش تراکنش" else "ثبت تراکنش",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }

    // Category Selection Sheet
    if (showCategoryPickerSheet) {
        val catType = if (selectedMode == TransactionMode.INCOME) "INCOME" else "EXPENSE"
        CategorySelectionBottomSheet(
            categoryType = catType,
            selectedCategoryName = selectedCategoryName,
            onCategorySelected = { cat ->
                selectedCategoryName = cat.name
                showCategoryPickerSheet = false
            },
            onDismiss = { showCategoryPickerSheet = false }
        )
    }

    // Account Picker Dialog
    pickingAccountForSource?.let { isSource ->
        AlertDialog(
            onDismissRequest = { pickingAccountForSource = null },
            title = {
                Text(
                    text = if (isSource) "انتخاب حساب مبدا" else "انتخاب حساب مقصد",
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            },
            containerColor = Color(0xFF1E293B),
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    bankAccounts.forEach { acc ->
                        val isCurrent = if (isSource) sourceAccount?.id == acc.id else destinationAccount?.id == acc.id
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .clickable {
                                    if (isSource) {
                                        sourceAccount = acc
                                    } else {
                                        destinationAccount = acc
                                    }
                                    pickingAccountForSource = null
                                },
                            color = if (isCurrent) Color(0xFF27354A) else Color(0xFF131B2A),
                            border = if (isCurrent) androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF38BDF8)) else null
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                BankLogoImage(
                                    bankName = acc.bankName,
                                    cardNumber = acc.cardNumber,
                                    containerSize = 40.dp,
                                    logoSize = 32.dp
                                )
                                Column {
                                    Text(acc.bankName, fontWeight = FontWeight.Bold, color = Color.White)
                                    Text(
                                        text = acc.cardNumber.takeLast(4).let { "کارت: **** $it" },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFF94A3B8)
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { pickingAccountForSource = null }) {
                    Text("انصراف", color = Color(0xFF94A3B8))
                }
            }
        )
    }
}
