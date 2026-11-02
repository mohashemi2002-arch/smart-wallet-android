package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.Category
import com.example.data.model.Transaction
import com.example.data.model.TransactionType
import com.example.ui.viewmodel.FinanceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetScreen(
    viewModel: FinanceViewModel = viewModel(),
    onBack: () -> Unit = {}
) {
    val categories by viewModel.categories.collectAsState()
    val transactions by viewModel.transactions.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            TopBarBudget(onBack = onBack)
        }

        item {
            BudgetOverviewCard(categories, transactions)
        }

        item {
            Text(
                text = "سیستم هشدار مصرف",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            categories.forEach { category ->
                CategoryRowCard(
                    category = category,
                    transactions = transactions,
                    onEditBudget = { viewModel.updateCategoryBudget(category, it) }
                )
            }
        }
    }
}

@Composable
private fun TopBarBudget(onBack: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "بازگشت")
        }
        Text(
            text = "بودجه",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun BudgetOverviewCard(
    categories: List<Category>,
    transactions: List<Transaction>
) {
    var totalBudget = 0L
    var totalSpent = 0L

    categories.forEach { category ->
        totalBudget += category.budgetLimitToman
        totalSpent += transactions
            .filter { it.categoryName == category.name && it.type == TransactionType.EXPENSE }
            .sumOf { it.amountToman }
    }

    val progress = if (totalBudget > 0) (totalSpent.toFloat() / totalBudget.toFloat()) else 0f
    val remaining = totalBudget - totalSpent

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "وضعیت کلی بودجه",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.8f)
            )
            Text(
                text = "${formatMoney(totalBudget)} تومان",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "مصرف شده: ${formatMoney(totalSpent)} تومان",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.8f)
            )

            LinearProgressIndicator(
                progress = { progress.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp),
                color = Color(0xFFB1E5D3),
                trackColor = Color(0xFF49454F)
            )

            Text(
                text = "مانده: ${formatMoney(remaining.coerceAtLeast(0L))} تومان",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
private fun CategoryRowCard(
    category: Category,
    transactions: List<Transaction>,
    onEditBudget: (Long) -> Unit
) {
    val spent = transactions
        .filter { it.categoryName == category.name && it.type == TransactionType.EXPENSE }
        .sumOf { it.amountToman }

    val ratio = if (category.budgetLimitToman > 0) {
        spent.toFloat() / category.budgetLimitToman.toFloat()
    } else {
        0f
    }

    val progressColor = when {
        ratio >= 1f -> Color(0xFFF2B8B5)
        ratio >= 0.8f -> Color(0xFFF4A340)
        else -> Color(0xFFB1E5D3)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = category.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = {
                    onEditBudget(category.budgetLimitToman)
                }) {
                    Icon(Icons.Default.Edit, contentDescription = "ویرایش سقف")
                }
            }

            LinearProgressIndicator(
                progress = { ratio.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = progressColor,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "مصرف شده: ${formatMoney(spent)} تومان",
                    style = MaterialTheme.typography.bodyMedium,
                    color = progressColor
                )
                Text(
                    text = "سقف: ${formatMoney(category.budgetLimitToman)} تومان",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (ratio >= 1f) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFF2B8B5))
                    Text(
                        text = "هشدار: از سقف تعیین شده گذشتید!",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFF2B8B5)
                    )
                }
            }
        }
    }
}

private fun formatMoney(value: Long): String {
    val rawValue = value.toString()
    val sign = if (rawValue.startsWith("-")) "-" else ""
    val digits = rawValue.removePrefix("-")

    val groupedDigits = digits
        .reversed()
        .chunked(3)
        .joinToString(",")
        .reversed()

    return sign + groupedDigits
}
