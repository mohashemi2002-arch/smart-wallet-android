package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.CategoryIconMap
import com.example.ui.components.PersianFormatters
import com.example.ui.components.formatAsToman

/**
 * Modern interactive Donut Chart for Expense distribution with center metrics.
 */
@Composable
fun ModernDonutChart(
    data: Map<String, Long>,
    totalAmount: Long,
    modifier: Modifier = Modifier
) {
    val total = if (totalAmount > 0) totalAmount else data.values.sum().coerceAtLeast(1L)
    val animatedProgress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(durationMillis = 800),
        label = "donut_anim"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
    ) {
        Canvas(
            modifier = Modifier
                .size(190.dp)
                .padding(12.dp)
        ) {
            val strokeWidth = 24.dp.toPx()
            val diameter = size.minDimension - strokeWidth
            val topLeft = Offset(
                x = (size.width - diameter) / 2f,
                y = (size.height - diameter) / 2f
            )
            val arcSize = Size(diameter, diameter)

            // Background subtle track
            drawArc(
                color = Color.LightGray.copy(alpha = 0.2f),
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth)
            )

            var currentAngle = -90f
            val entries = data.entries.toList()
            val gapAngle = if (entries.size > 1) 3f else 0f
            val totalGaps = gapAngle * entries.size
            val availableDegrees = (360f - totalGaps).coerceAtLeast(1f)

            entries.forEach { entry ->
                val (catColor, _) = CategoryIconMap.getIconForCategory(entry.key)
                val rawSweep = (entry.value.toFloat() / total) * availableDegrees
                val sweepAngle = rawSweep * animatedProgress

                if (sweepAngle > 0.5f) {
                    drawArc(
                        color = catColor,
                        startAngle = currentAngle + (gapAngle / 2f),
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }
                currentAngle += (rawSweep + gapAngle)
            }
        }

        // Center Content
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        ) {
            Text(
                text = "مجموع هزینه‌ها",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = totalAmount.formatAsToman(),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                ),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "تومان",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Breakdown list of top spending categories with custom progress bars and icons.
 */
@Composable
fun CategoryExpenseBreakdown(
    data: Map<String, Long>,
    totalExpense: Long,
    modifier: Modifier = Modifier,
    maxItemsToShow: Int = 5
) {
    val total = if (totalExpense > 0) totalExpense else data.values.sum().coerceAtLeast(1L)
    val sortedCategories = data.entries
        .sortedByDescending { it.value }
        .take(maxItemsToShow)

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        sortedCategories.forEach { (categoryName, amount) ->
            val (iconColor, iconVector) = CategoryIconMap.getIconForCategory(categoryName)
            val percentage = ((amount.toDouble() / total.toDouble()) * 100).toInt()
            val progressFraction = (amount.toFloat() / total.toFloat()).coerceIn(0f, 1f)

            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(iconColor.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = iconVector,
                                contentDescription = categoryName,
                                tint = iconColor,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Text(
                            text = categoryName,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "${amount.formatAsToman()} تومان",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(iconColor.copy(alpha = 0.12f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "${PersianFormatters.toPersianDigits(percentage.toString())}٪",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = iconColor
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Progress Bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fraction = progressFraction)
                            .fillMaxHeight()
                            .clip(CircleShape)
                            .background(iconColor)
                    )
                }
            }
        }
    }
}

/**
 * Backward-compatible SimpleDonutChart wrapper so existing callers never break.
 */
@Composable
fun SimpleDonutChart(
    data: Map<String, Long>,
    colors: List<Color>,
    modifier: Modifier = Modifier
) {
    val total = data.values.sum().coerceAtLeast(0L)
    ModernDonutChart(
        data = data,
        totalAmount = total,
        modifier = modifier
    )
}
