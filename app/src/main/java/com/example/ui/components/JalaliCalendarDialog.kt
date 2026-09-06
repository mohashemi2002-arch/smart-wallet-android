package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.util.JalaliCalendarHelper
import com.example.ui.viewmodel.FinanceViewModel.TransactionDateFilter
import com.example.data.model.Transaction
import com.example.data.model.TransactionType

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun JalaliCalendarDialog(
    initialFilter: TransactionDateFilter?,
    transactions: List<Transaction>, // For dot indicators
    onFilterSelected: (TransactionDateFilter) -> Unit,
    onDismiss: () -> Unit
) {
    val currentJalali = JalaliCalendarHelper.getJalaliDateFromMillis(System.currentTimeMillis())

    var viewedYear by remember { mutableStateOf(currentJalali.year) }
    var viewedMonth by remember { mutableStateOf(currentJalali.month) }

    var selectedFilter by remember { mutableStateOf<TransactionDateFilter?>(initialFilter) }

    // Init viewed month to the selected filter if exists
    LaunchedEffect(initialFilter) {
        if (initialFilter != null) {
            when (initialFilter) {
                is TransactionDateFilter.DayFilter -> {
                    viewedYear = initialFilter.jYear
                    viewedMonth = initialFilter.jMonth
                }
                is TransactionDateFilter.MonthFilter -> {
                    viewedYear = initialFilter.jYear
                    viewedMonth = initialFilter.jMonth
                }
                is TransactionDateFilter.AllTime -> {}
            }
        }
    }

    val daysInMonth = JalaliCalendarHelper.getMonthLength(viewedYear, viewedMonth)
    val firstDayOfMonthMillis = JalaliCalendarHelper.getMillisFromJalali(viewedYear, viewedMonth, 1)
    val firstDayOfWeek = JalaliCalendarHelper.getJalaliDateFromMillis(firstDayOfMonthMillis).dayOfWeekName
    
    val weekDays = arrayOf("شنبه", "یکشنبه", "دوشنبه", "سه‌شنبه", "چهارشنبه", "پنج‌شنبه", "جمعه")
    val weekDayInitials = arrayOf("ش", "ی", "د", "س", "چ", "پ", "ج")
    val emptyCellsBefore = weekDays.indexOf(firstDayOfWeek)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .wrapContentHeight(),
            shape = RoundedCornerShape(28.dp),
            color = Color(0xFF0F172A),
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Display
                val displayTitle = when (val filter = selectedFilter) {
                    is TransactionDateFilter.DayFilter -> {
                        if (filter.jYear == currentJalali.year && filter.jMonth == currentJalali.month && filter.jDay == currentJalali.day) {
                            "امروز - ${filter.jDay} ${JalaliCalendarHelper.monthNames[filter.jMonth - 1]}"
                        } else {
                            val tempMillis = JalaliCalendarHelper.getMillisFromJalali(filter.jYear, filter.jMonth, filter.jDay)
                            val d = JalaliCalendarHelper.getJalaliDateFromMillis(tempMillis)
                            "${d.dayOfWeekName} ${d.day} ${d.monthName} ${d.year}"
                        }
                    }
                    is TransactionDateFilter.MonthFilter -> "${JalaliCalendarHelper.monthNames[filter.jMonth - 1]} ${filter.jYear}"
                    is TransactionDateFilter.AllTime -> "کل تراکنش‌ها"
                    null -> "انتخاب تاریخ تراکنش‌ها"
                }

                Text(
                    text = "انتخاب تاریخ تراکنش‌ها",
                    color = Color(0xFF94A3B8),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = displayTitle,
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 6.dp, bottom = 20.dp)
                )

                // Month Navigator
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF1E293B), RoundedCornerShape(16.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {
                        if (viewedMonth == 12) {
                            viewedMonth = 1
                            viewedYear++
                        } else {
                            viewedMonth++
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "ماه بعد", tint = Color.White)
                    }
                    
                    AnimatedContent(
                        targetState = "${JalaliCalendarHelper.monthNames[viewedMonth - 1]} $viewedYear",
                        transitionSpec = {
                            fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(90))
                        }, label = "monthYearAnim"
                    ) { targetText ->
                        Text(
                            text = targetText,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                    
                    IconButton(onClick = {
                        if (viewedMonth == 1) {
                            viewedMonth = 12
                            viewedYear--
                        } else {
                            viewedMonth--
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "ماه قبل", tint = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Grid Headers
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    weekDayInitials.forEachIndexed { index, dayChar ->
                        val isFriday = index == 6
                        Text(
                            text = dayChar,
                            color = if (isFriday) Color(0xFFF87171) else Color(0xFF94A3B8),
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Days Grid
                LazyVerticalGrid(
                    columns = GridCells.Fixed(7),
                    modifier = Modifier.height(260.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(emptyCellsBefore) {
                        Box(modifier = Modifier.aspectRatio(1f))
                    }
                    items(daysInMonth) { dayIndex ->
                        val day = dayIndex + 1
                        val isToday = currentJalali.year == viewedYear && currentJalali.month == viewedMonth && currentJalali.day == day
                        
                        val isSelected = when (val filter = selectedFilter) {
                            is TransactionDateFilter.DayFilter -> filter.jYear == viewedYear && filter.jMonth == viewedMonth && filter.jDay == day
                            is TransactionDateFilter.MonthFilter -> false
                            is TransactionDateFilter.AllTime -> false
                            null -> false
                        }

                        // Check for transactions
                        val startOfDay = JalaliCalendarHelper.getMillisFromJalali(viewedYear, viewedMonth, day, 0, 0, 0)
                        val endOfDay = JalaliCalendarHelper.getMillisFromJalali(viewedYear, viewedMonth, day, 23, 59, 59)
                        
                        val dayTxs = transactions.filter { it.dateMillis in startOfDay..endOfDay }
                        val hasIncome = dayTxs.any { it.type == TransactionType.INCOME }
                        val hasExpense = dayTxs.any { it.type == TransactionType.EXPENSE }

                        val bgColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
                        val textColor = if (isSelected) Color.White else if (isToday) MaterialTheme.colorScheme.primary else Color(0xFFF1F5F9)
                        val borderModifier = if (isToday && !isSelected) Modifier.border(1.dp, MaterialTheme.colorScheme.primary, CircleShape) else Modifier

                        Box(
                            modifier = Modifier
                                .aspectRatio(1f)
                                .then(borderModifier)
                                .clip(CircleShape)
                                .background(bgColor)
                                .clickable {
                                    val (start, end) = JalaliCalendarHelper.getStartAndEndOfJalaliDay(viewedYear, viewedMonth, day)
                                    selectedFilter = TransactionDateFilter.DayFilter(viewedYear,  viewedMonth,  day)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                                Text(
                                    text = PersianFormatters.toPersianDigits(day.toString()),
                                    color = textColor,
                                    fontSize = 15.sp,
                                    fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal
                                )
                                // Transaction Indicator Dots
                                if (dayTxs.isNotEmpty() && !isSelected) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp), modifier = Modifier.padding(top = 2.dp)) {
                                        if (hasIncome) {
                                            Box(modifier = Modifier.size(4.dp).background(Color(0xFF4ADE80), CircleShape))
                                        }
                                        if (hasExpense) {
                                            Box(modifier = Modifier.size(4.dp).background(Color(0xFFF87171), CircleShape))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                
                // Quick Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    AssistChip(
                        onClick = {
                            val (start, end) = JalaliCalendarHelper.getStartAndEndOfJalaliDay(currentJalali.year, currentJalali.month, currentJalali.day)
                            selectedFilter = TransactionDateFilter.DayFilter(currentJalali.year,  currentJalali.month,  currentJalali.day)
                            viewedYear = currentJalali.year
                            viewedMonth = currentJalali.month
                        },
                        label = { Text("امروز", fontSize = 12.sp) },
                        colors = AssistChipDefaults.assistChipColors(containerColor = Color(0xFF1E293B), labelColor = Color.White),
                        border = AssistChipDefaults.assistChipBorder(enabled = true, borderColor = Color.Transparent)
                    )
                    AssistChip(
                        onClick = {
                            val yesterdayMillis = System.currentTimeMillis() - 86400000L
                            val yJalali = JalaliCalendarHelper.getJalaliDateFromMillis(yesterdayMillis)
                            val (start, end) = JalaliCalendarHelper.getStartAndEndOfJalaliDay(yJalali.year, yJalali.month, yJalali.day)
                            selectedFilter = TransactionDateFilter.DayFilter(yJalali.year,  yJalali.month,  yJalali.day)
                            viewedYear = yJalali.year
                            viewedMonth = yJalali.month
                        },
                        label = { Text("دیروز", fontSize = 12.sp) },
                        colors = AssistChipDefaults.assistChipColors(containerColor = Color(0xFF1E293B), labelColor = Color.White),
                        border = AssistChipDefaults.assistChipBorder(enabled = true, borderColor = Color.Transparent)
                    )
                    AssistChip(
                        onClick = {
                            val (start, end) = JalaliCalendarHelper.getStartAndEndOfJalaliMonth(viewedYear, viewedMonth)
                            selectedFilter = TransactionDateFilter.MonthFilter(viewedYear,  viewedMonth)
                        },
                        label = { Text("کل این ماه", fontSize = 12.sp) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = if (selectedFilter is TransactionDateFilter.MonthFilter) MaterialTheme.colorScheme.primary else Color(0xFF1E293B),
                            labelColor = Color.White
                        ),
                        border = AssistChipDefaults.assistChipBorder(enabled = true, borderColor = Color.Transparent)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("انصراف", color = Color(0xFFF87171))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            selectedFilter?.let { onFilterSelected(it) }
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        enabled = selectedFilter != null
                    ) {
                        Text("تایید و مشاهده")
                    }
                }
            }
        }
    }
}
