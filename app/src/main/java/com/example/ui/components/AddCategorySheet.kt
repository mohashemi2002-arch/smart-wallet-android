package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.repository.CategoryRepository
import com.example.data.repository.TransactionCategory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCategoryDialog(
    categoryType: String, // "EXPENSE" or "INCOME"
    categoryToEdit: TransactionCategory? = null,
    onDismiss: () -> Unit,
    onCategorySaved: (TransactionCategory) -> Unit
) {
    val context = LocalContext.current
    val repository = remember { CategoryRepository(context) }

    var categoryName by remember { mutableStateOf(categoryToEdit?.name ?: "") }
    var selectedIconKey by remember { mutableStateOf(categoryToEdit?.iconKey ?: "snowflake") }
    var selectedColorHex by remember { mutableStateOf(categoryToEdit?.colorHex ?: "#84CC16") }

    var showActionSheet by remember { mutableStateOf(false) }
    var showIconPickerSheet by remember { mutableStateOf(false) }
    var showColorPickerSheet by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val activeColor = remember(selectedColorHex) {
        CategoryIconMap.parseColor(selectedColorHex, Color(0xFF84CC16))
    }
    val activeVector = remember(selectedIconKey) {
        CategoryIconMap.getVectorForIconKey(selectedIconKey)
    }

    val typeTitle = if (categoryType.equals("INCOME", ignoreCase = true)) "دخل" else "خرج"
    val dialogTitle = if (categoryToEdit != null) "ویرایش دسته‌بندی $typeTitle" else "افزودن دسته‌بندی $typeTitle"

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = dialogTitle,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            ),
                            color = Color(0xFFE2E8F0)
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "بازگشت",
                                tint = Color(0xFFE2E8F0)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF131826))
                )
            },
            containerColor = Color(0xFF131826)
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(20.dp))

                // Large Preview Box
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color(0xFF1C2436))
                        .border(1.dp, Color(0xFF2C384E), RoundedCornerShape(24.dp))
                        .clickable { showActionSheet = true },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = activeVector,
                        contentDescription = "آیکون دسته‌بندی",
                        tint = activeColor,
                        modifier = Modifier.size(68.dp)
                    )

                    // Floating Edit Badge
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(8.dp)
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF2E3B52)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "ویرایش آیکون یا رنگ",
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Name Input Field
                OutlinedTextField(
                    value = categoryName,
                    onValueChange = {
                        categoryName = it
                        if (errorMessage != null) errorMessage = null
                    },
                    label = { Text("نام", color = Color(0xFF94A3B8)) },
                    placeholder = { Text("نام دسته‌بندی را وارد کنید", color = Color(0xFF64748B)) },
                    singleLine = true,
                    isError = errorMessage != null,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
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
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage ?: "",
                        color = Color(0xFFEF4444),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.align(Alignment.Start)
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // Submit Button
                Button(
                    onClick = {
                        val trimmed = categoryName.trim()
                        if (trimmed.isBlank()) {
                            errorMessage = "لطفاً نام دسته‌بندی را وارد کنید"
                            return@Button
                        }

                        val saved = repository.saveCategory(
                            id = categoryToEdit?.id,
                            name = trimmed,
                            type = categoryType,
                            iconKey = selectedIconKey,
                            colorHex = selectedColorHex
                        )
                        onCategorySaved(saved)
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
                ) {
                    Text(
                        text = "ثبت",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }

    // Action Sheet: Choose between "تغییر شکلک" and "تغییر رنگ"
    if (showActionSheet) {
        ModalBottomSheet(
            onDismissRequest = { showActionSheet = false },
            containerColor = Color(0xFF1E293B),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Change Icon Option
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .clickable {
                            showActionSheet = false
                            showIconPickerSheet = true
                        },
                    color = Color(0xFF27354A)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "تغییر شکلک",
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                            color = Color.White
                        )
                        Icon(
                            imageVector = activeVector,
                            contentDescription = null,
                            tint = activeColor,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                // Change Color Option
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .clickable {
                            showActionSheet = false
                            showColorPickerSheet = true
                        },
                    color = Color(0xFF27354A)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "تغییر رنگ",
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                            color = Color.White
                        )
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(activeColor)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }

    // Icon Picker Sheet (5 Columns Grid)
    if (showIconPickerSheet) {
        ModalBottomSheet(
            onDismissRequest = { showIconPickerSheet = false },
            containerColor = Color(0xFF1E293B),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "شکلک‌ها",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                Spacer(modifier = Modifier.height(16.dp))

                LazyVerticalGrid(
                    columns = GridCells.Fixed(5),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(360.dp),
                    contentPadding = PaddingValues(8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(CategoryIconMap.AVAILABLE_ICONS) { (iconKey, vector) ->
                        val isSelected = iconKey.equals(selectedIconKey, ignoreCase = true)
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) activeColor else Color.Transparent)
                                .clickable {
                                    selectedIconKey = iconKey
                                    showIconPickerSheet = false
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = vector,
                                contentDescription = iconKey,
                                tint = if (isSelected) Color(0xFF0F172A) else Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }

    // Color Picker Sheet (5x5 Grid of 25 Curated Colors)
    if (showColorPickerSheet) {
        ModalBottomSheet(
            onDismissRequest = { showColorPickerSheet = false },
            containerColor = Color(0xFF1E293B),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "رنگ دسته‌بندی",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                Spacer(modifier = Modifier.height(20.dp))

                LazyVerticalGrid(
                    columns = GridCells.Fixed(5),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                    horizontalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    items(CategoryIconMap.PALETTE_25_COLORS) { colorItem ->
                        val hex = CategoryIconMap.colorToHex(colorItem)
                        val isSelected = hex.equals(selectedColorHex, ignoreCase = true)

                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .then(
                                    if (isSelected) {
                                        Modifier.border(3.dp, colorItem, CircleShape)
                                    } else Modifier
                                )
                                .padding(if (isSelected) 4.dp else 0.dp)
                                .clip(CircleShape)
                                .background(colorItem)
                                .clickable {
                                    selectedColorHex = hex
                                    showColorPickerSheet = false
                                },
                            contentAlignment = Alignment.Center
                        ) {}
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}
