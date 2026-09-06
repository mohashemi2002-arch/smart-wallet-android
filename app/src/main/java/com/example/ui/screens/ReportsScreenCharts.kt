package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
fun SimpleDonutChart(data: Map<String, Long>, colors: List<Color>, modifier: Modifier = Modifier) {
    val total = data.values.sum().coerceAtLeast(1L)
    var startAngle = -90f
    
    Box(contentAlignment = Alignment.Center, modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 30.dp.toPx()
            val radius = size.minDimension / 2 - strokeWidth
            
            data.values.forEachIndexed { index, value ->
                val sweepAngle = (value.toFloat() / total) * 360f
                drawArc(
                    color = colors[index % colors.size],
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    topLeft = Offset(strokeWidth, strokeWidth),
                    size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
                    style = Stroke(width = strokeWidth)
                )
                startAngle += sweepAngle
            }
        }
        Text("نمودار هزینه‌ها")
    }
}
