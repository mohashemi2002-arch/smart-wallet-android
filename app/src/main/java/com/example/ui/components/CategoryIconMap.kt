package com.example.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

object CategoryIconMap {

    /**
     * Curated 5x5 color palette (25 colors) exactly matching the Iranian financial app UI design
     */
    val PALETTE_25_COLORS = listOf(
        // Row 1: Lime & Greens
        Color(0xFF84CC16), Color(0xFFA3E635), Color(0xFFCCFF00), Color(0xFF22C55E), Color(0xFF15803D),
        // Row 2: Cyans, Light Blues & Teals
        Color(0xFF06B6D4), Color(0xFF38BDF8), Color(0xFF818CF8), Color(0xFF2563EB), Color(0xFF0D9488),
        // Row 3: Blue, Pinks & Purples
        Color(0xFF3B82F6), Color(0xFFF472B6), Color(0xFFEC4899), Color(0xFFD946EF), Color(0xFF8B5CF6),
        // Row 4: Oranges, Yellows & Reds
        Color(0xFFFB923C), Color(0xFFFBBF24), Color(0xFFF97316), Color(0xFFEF4444), Color(0xFFBE185D),
        // Row 5: Earth tones, Slates & Neutrals
        Color(0xFFB45309), Color(0xFFE2E8F0), Color(0xFF94A3B8), Color(0xFF64748B), Color(0xFF475569)
    )

    /**
     * Curated selection of vector icons for the 5-column grid in Category Creation/Editing
     */
    val AVAILABLE_ICONS: List<Pair<String, ImageVector>> = listOf(
        "snowflake" to Icons.Default.AcUnit,
        "cash" to Icons.Default.AttachMoney,
        "food" to Icons.Default.Fastfood,
        "fuel" to Icons.Default.LocalGasStation,
        "car" to Icons.Default.DirectionsCar,

        "home" to Icons.Default.Home,
        "toll" to Icons.Default.ConfirmationNumber,
        "smoke" to Icons.Default.SmokingRooms,
        "shopping" to Icons.Default.ShoppingBag,
        "health" to Icons.Default.LocalHospital,

        "receipt" to Icons.Default.Receipt,
        "payments" to Icons.Default.Payments,
        "gift" to Icons.Default.Redeem,
        "bank" to Icons.Default.AccountBalance,
        "heart" to Icons.Default.Favorite,

        "transfer" to Icons.Default.SwapHoriz,
        "wallet" to Icons.Default.AccountBalanceWallet,
        "cafe" to Icons.Default.LocalCafe,
        "school" to Icons.Default.School,
        "entertainment" to Icons.Default.SportsEsports,

        "work" to Icons.Default.Work,
        "savings" to Icons.Default.Savings,
        "pets" to Icons.Default.Pets,
        "wifi" to Icons.Default.Wifi,
        "phone" to Icons.Default.PhoneIphone,

        "camera" to Icons.Default.CameraAlt,
        "mic" to Icons.Default.Mic,
        "fitness" to Icons.Default.FitnessCenter,
        "time" to Icons.Default.AccessTime,
        "person" to Icons.Default.Person,

        "build" to Icons.Default.Build,
        "flight" to Icons.Default.Flight,
        "star" to Icons.Default.Star,
        "settings" to Icons.Default.Settings,
        "cake" to Icons.Default.Cake
    )

    fun getVectorForIconKey(iconKey: String): ImageVector {
        return AVAILABLE_ICONS.firstOrNull { it.first.equals(iconKey, ignoreCase = true) }?.second
            ?: when (iconKey.lowercase()) {
                "snowflake", "ac_unit" -> Icons.Default.AcUnit
                "cash" -> Icons.Default.AttachMoney
                "food", "restaurant" -> Icons.Default.Fastfood
                "fuel", "gas" -> Icons.Default.LocalGasStation
                "car" -> Icons.Default.DirectionsCar
                "home" -> Icons.Default.Home
                "toll" -> Icons.Default.ConfirmationNumber
                "smoke" -> Icons.Default.SmokingRooms
                "shopping" -> Icons.Default.ShoppingBag
                "health" -> Icons.Default.LocalHospital
                "receipt" -> Icons.Default.Receipt
                "payments", "salary" -> Icons.Default.Payments
                "gift", "reward" -> Icons.Default.Redeem
                "bank" -> Icons.Default.AccountBalance
                "heart", "father" -> Icons.Default.Favorite
                "transfer" -> Icons.Default.SwapHoriz
                "wallet" -> Icons.Default.AccountBalanceWallet
                "cafe" -> Icons.Default.LocalCafe
                "school", "edu" -> Icons.Default.School
                "entertainment" -> Icons.Default.SportsEsports
                else -> Icons.Default.Category
            }
    }

    fun parseColor(colorHex: String, defaultColor: Color = Color(0xFF84CC16)): Color {
        return try {
            val cleanHex = colorHex.trim().removePrefix("#")
            val colorLong = when (cleanHex.length) {
                6 -> "FF$cleanHex".toLong(16)
                8 -> cleanHex.toLong(16)
                else -> return defaultColor
            }
            Color(colorLong)
        } catch (e: Exception) {
            defaultColor
        }
    }

    fun colorToHex(color: Color): String {
        val argb = (color.value.toLong() shr 32).toInt()
        val r = (color.red * 255).toInt()
        val g = (color.green * 255).toInt()
        val b = (color.blue * 255).toInt()
        return String.format("#%02X%02X%02X", r, g, b)
    }

    /**
     * Backward-compatible helper for legacy screens
     */
    fun getIconForCategory(categoryName: String): Pair<Color, ImageVector> {
        return when (categoryName.trim()) {
            "خوراکی و سوپرمارکت", "خوردنی" -> Color(0xFFF97316) to Icons.Default.Fastfood
            "حمل و نقل", "خودرو" -> Color(0xFF06B6D4) to Icons.Default.DirectionsCar
            "بنزین" -> Color(0xFFEF4444) to Icons.Default.LocalGasStation
            "مسکن و اجاره", "خونه", "خانه" -> Color(0xFF3B82F6) to Icons.Default.Home
            "قبوض و خدمات", "قبوض" -> Color(0xFF14B8A6) to Icons.Default.Receipt
            "سلامت و درمان" -> Color(0xFF10B981) to Icons.Default.LocalHospital
            "پوشاک و خرید", "پوشاک" -> Color(0xFF8B5CF6) to Icons.Default.ShoppingBag
            "تفریح و سرگرمی" -> Color(0xFF3B82F6) to Icons.Default.SportsEsports
            "پس‌انداز و سرمایه" -> Color(0xFF84CC16) to Icons.Default.Savings
            "درآمد و حقوق", "حقوق" -> Color(0xFF22C55E) to Icons.Default.Payments
            "پاداش" -> Color(0xFF10B981) to Icons.Default.Redeem
            "سایر دخل‌ها" -> Color(0xFF14B8A6) to Icons.Default.AccountBalance
            "نقدی" -> Color(0xFFF59E0B) to Icons.Default.AttachMoney
            "پدر" -> Color(0xFF22C55E) to Icons.Default.Favorite
            "عوارض آزادراهی" -> Color(0xFFEC4899) to Icons.Default.ConfirmationNumber
            "سیگار" -> Color(0xFFA855F7) to Icons.Default.SmokingRooms
            "آموزش" -> Color(0xFF6366F1) to Icons.Default.School
            "جابجایی", "جابجایی داخلی" -> Color(0xFF3B82F6) to Icons.Default.SwapHoriz
            else -> Color(0xFF607D8B) to Icons.Default.Category
        }
    }
}
