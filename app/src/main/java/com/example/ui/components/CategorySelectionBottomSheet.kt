package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import com.example.data.repository.CategoryRepository
import com.example.data.repository.TransactionCategory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategorySelectionBottomSheet(
    categoryType: String, // "EXPENSE" or "INCOME"
    selectedCategoryName: String?,
    onCategorySelected: (TransactionCategory) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val repository = remember { CategoryRepository(context) }
    val allCategories by repository.categoriesFlow.collectAsState()

    val categories = remember(allCategories, categoryType) {
        repository.getCategories(categoryType)
    }

    var isEditMode by remember { mutableStateOf(false) }
    var categoryToEdit by remember { mutableStateOf<TransactionCategory?>(null) }
    var showAddCategoryDialog by remember { mutableStateOf(false) }

    val typeLabel = if (categoryType.equals("INCOME", ignoreCase = true)) "دخل" else "خرج"

    if (showAddCategoryDialog) {
        AddCategoryDialog(
            categoryType = categoryType,
            categoryToEdit = categoryToEdit,
            onDismiss = {
                showAddCategoryDialog = false
                categoryToEdit = null
            },
            onCategorySaved = { saved ->
                showAddCategoryDialog = false
                categoryToEdit = null
                onCategorySelected(saved)
                onDismiss()
            }
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF131B2A),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Header with title and edit toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp, start = 8.dp, end = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = { isEditMode = !isEditMode }) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "ویرایش دسته‌بندی‌ها",
                        tint = if (isEditMode) Color(0xFF38BDF8) else Color(0xFF94A3B8)
                    )
                }

                Text(
                    text = "دسته‌بندی",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    ),
                    color = Color.White
                )

                // Placeholder to balance the row
                Spacer(modifier = Modifier.size(40.dp))
            }

            // Categories List
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(vertical = 4.dp)
            ) {
                items(categories, key = { it.id }) { cat ->
                    val isSelected = cat.name.equals(selectedCategoryName, ignoreCase = true)
                    val catColor = CategoryIconMap.parseColor(cat.colorHex, Color(0xFF22C55E))
                    val catVector = CategoryIconMap.getVectorForIconKey(cat.iconKey)

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .clickable {
                                if (isEditMode) {
                                    categoryToEdit = cat
                                    showAddCategoryDialog = true
                                } else {
                                    onCategorySelected(cat)
                                    onDismiss()
                                }
                            },
                        color = if (isSelected) Color(0xFF243247) else Color(0xFF1A2333),
                        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF38BDF8)) else null
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Left side: Icon
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                if (isEditMode) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "ویرایش",
                                        tint = Color(0xFF38BDF8),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(catColor.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = catVector,
                                        contentDescription = cat.name,
                                        tint = catColor,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }

                            // Right side: Name
                            Text(
                                text = cat.name,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                ),
                                color = if (isSelected) Color(0xFF38BDF8) else Color(0xFFF1F5F9)
                            )
                        }
                    }
                }

                // Add Category Card at the bottom
                item {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .clickable {
                                categoryToEdit = null
                                showAddCategoryDialog = true
                            },
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
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "افزودن دسته‌بندی",
                                tint = Color(0xFF38BDF8),
                                modifier = Modifier.size(22.dp)
                            )

                            Text(
                                text = "+ افزودن دسته‌بندی $typeLabel",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = Color(0xFF38BDF8)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
