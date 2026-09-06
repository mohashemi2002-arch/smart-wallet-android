package com.example.ui.components

import androidx.compose.foundation.background
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

@Composable
fun JalaliDatePickerDialog(
    initialMillis: Long? = null,
    onDateSelected: (startMillis: Long, endMillis: Long, title: String) -> Unit,
    onDismiss: () -> Unit
) {
    val currentJalali = JalaliCalendarHelper.getJalaliDateFromMillis(System.currentTimeMillis())
    
    val selectedMillis = initialMillis ?: System.currentTimeMillis()
    val initialSelected = JalaliCalendarHelper.getJalaliDateFromMillis(selectedMillis)

    var viewedYear by remember { mutableStateOf(initialSelected.year) }
    var viewedMonth by remember { mutableStateOf(initialSelected.month) }

    var selectedYear by remember { mutableStateOf(initialSelected.year) }
    var selectedMonth by remember { mutableStateOf(initialSelected.month) }
    var selectedDay by remember { mutableStateOf(initialSelected.day) }

    var isFullMonthSelected by remember { mutableStateOf(false) }

    val daysInMonth = JalaliCalendarHelper.getMonthLength(viewedYear, viewedMonth)
    val firstDayOfMonthMillis = JalaliCalendarHelper.getMillisFromJalali(viewedYear, viewedMonth, 1)
    val firstDayOfWeek = JalaliCalendarHelper.getJalaliDateFromMillis(firstDayOfMonthMillis).dayOfWeekName
    
    // sh, y, d, s, ch, p, j
    val weekDays = arrayOf("شنبه", "یکشنبه", "دوشنبه", "سه‌شنبه", "چهارشنبه", "پنج‌شنبه", "جمعه")
    val weekDayInitials = arrayOf("ش", "ی", "د", "س", "چ", "پ", "ج")
    val emptyCellsBefore = weekDays.indexOf(firstDayOfWeek)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .wrapContentHeight(),
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFF1E2638)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Display
                val displayTitle = if (isFullMonthSelected) {
                    "${JalaliCalendarHelper.monthNames[selectedMonth - 1]} $selectedYear"
                } else {
                    val tempMillis = JalaliCalendarHelper.getMillisFromJalali(selectedYear, selectedMonth, selectedDay)
                    val d = JalaliCalendarHelper.getJalaliDateFromMillis(tempMillis)
                    "${d.dayOfWeekName} ${d.day} ${d.monthName} ${d.year}"
                }

                Text(
                    text = "انتخاب تاریخ",
                    color = Color.Gray,
                    fontSize = 12.sp
                )
                Text(
                    text = displayTitle,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                )

                // Quick Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    AssistChip(
                        onClick = {
                            selectedYear = currentJalali.year
                            selectedMonth = currentJalali.month
                            selectedDay = currentJalali.day
                            viewedYear = currentJalali.year
                            viewedMonth = currentJalali.month
                            isFullMonthSelected = false
                        },
                        label = { Text("امروز") },
                        colors = AssistChipDefaults.assistChipColors(containerColor = Color(0xFF2C374D), labelColor = Color.White)
                    )
                    AssistChip(
                        onClick = {
                            val yesterdayMillis = System.currentTimeMillis() - 86400000L
                            val yJalali = JalaliCalendarHelper.getJalaliDateFromMillis(yesterdayMillis)
                            selectedYear = yJalali.year
                            selectedMonth = yJalali.month
                            selectedDay = yJalali.day
                            viewedYear = yJalali.year
                            viewedMonth = yJalali.month
                            isFullMonthSelected = false
                        },
                        label = { Text("دیروز") },
                        colors = AssistChipDefaults.assistChipColors(containerColor = Color(0xFF2C374D), labelColor = Color.White)
                    )
                    AssistChip(
                        onClick = {
                            selectedYear = viewedYear
                            selectedMonth = viewedMonth
                            isFullMonthSelected = true
                        },
                        label = { Text("کل این ماه") },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = if (isFullMonthSelected) Color(0xFF4A6DA7) else Color(0xFF2C374D),
                            labelColor = Color.White
                        )
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Month Navigator
                Row(
                    modifier = Modifier.fillMaxWidth(),
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
                    Text(
                        text = "${JalaliCalendarHelper.monthNames[viewedMonth - 1]} $viewedYear",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
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

                // Grid Headers
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    weekDayInitials.forEach { dayChar ->
                        Text(
                            text = dayChar,
                            color = Color.Gray,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center,
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Days Grid
                LazyVerticalGrid(
                    columns = GridCells.Fixed(7),
                    modifier = Modifier.height(250.dp)
                ) {
                    items(emptyCellsBefore) {
                        Box(modifier = Modifier.aspectRatio(1f))
                    }
                    items(daysInMonth) { dayIndex ->
                        val day = dayIndex + 1
                        val isToday = currentJalali.year == viewedYear && currentJalali.month == viewedMonth && currentJalali.day == day
                        val isSelected = !isFullMonthSelected && selectedYear == viewedYear && selectedMonth == viewedMonth && selectedDay == day

                        val bgColor = if (isSelected) Color(0xFF4A6DA7) else if (isToday) Color(0xFF333E52) else Color.Transparent
                        val textColor = if (isSelected) Color.White else if (isToday) Color(0xFF4A6DA7) else Color.White

                        Box(
                            modifier = Modifier
                                .aspectRatio(1f)
                                .padding(2.dp)
                                .clip(CircleShape)
                                .background(bgColor)
                                .clickable {
                                    selectedYear = viewedYear
                                    selectedMonth = viewedMonth
                                    selectedDay = day
                                    isFullMonthSelected = false
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = day.toString(),
                                color = textColor,
                                fontSize = 14.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("انصراف", color = Color(0xFFE57373))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (isFullMonthSelected) {
                                val (start, end) = JalaliCalendarHelper.getStartAndEndOfJalaliMonth(selectedYear, selectedMonth)
                                val title = "${JalaliCalendarHelper.monthNames[selectedMonth - 1]} $selectedYear"
                                onDateSelected(start, end, title)
                            } else {
                                val (start, end) = JalaliCalendarHelper.getStartAndEndOfJalaliDay(selectedYear, selectedMonth, selectedDay)
                                val tempMillis = JalaliCalendarHelper.getMillisFromJalali(selectedYear, selectedMonth, selectedDay)
                                val d = JalaliCalendarHelper.getJalaliDateFromMillis(tempMillis)
                                
                                val title = if (selectedYear == currentJalali.year && selectedMonth == currentJalali.month && selectedDay == currentJalali.day) {
                                    "امروز - ${d.day} ${d.monthName}"
                                } else {
                                    "${d.dayOfWeekName} ${d.day} ${d.monthName} ${d.year}"
                                }
                                onDateSelected(start, end, title)
                            }
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4A6DA7))
                    ) {
                        Text("تایید و اعمال فیلتر")
                    }
                }
            }
        }
    }
}
