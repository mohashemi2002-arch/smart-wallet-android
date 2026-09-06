package com.example.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

object CategoryIconMap {
    fun getIconForCategory(categoryName: String): Pair<Color, ImageVector> {
        return when (categoryName) {
            "خوراکی و سوپرمارکت" -> Color(0xFFFF5722) to Icons.Default.Restaurant
            "حمل و نقل" -> Color(0xFF0288D1) to Icons.Default.DirectionsCar
            "مسکن و اجاره" -> Color(0xFF7B1FA2) to Icons.Default.Home
            "قبوض و خدمات" -> Color(0xFF00897B) to Icons.Default.Receipt
            "سلامت و درمان" -> Color(0xFFE91E63) to Icons.Default.LocalHospital
            "پوشاک و خرید" -> Color(0xFFF57C00) to Icons.Default.ShoppingBag
            "تفریح و سرگرمی" -> Color(0xFF3F51B5) to Icons.Default.Movie
            "پس‌انداز و سرمایه" -> Color(0xFF388E3C) to Icons.Default.Savings
            "درآمد و حقوق" -> Color(0xFF81C784) to Icons.Default.Payments
            else -> Color(0xFF607D8B) to Icons.Default.Category
        }
    }
}
