package com.example.data

import com.example.data.model.Asset
import com.example.data.model.Transaction
import com.example.data.model.TransactionType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object Exporter {
    fun generateExportCsv(
        transactions: List<Transaction>,
        assets: List<Asset>
    ): String {
        val sb = StringBuilder()
        val dateFormat = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.US)
        sb.append("=== گزارش مالی اسمارت ولت AI (Smart Wallet Financial Export) ===\n")
        sb.append("تاریخ خروجی: ").append(dateFormat.format(Date())).append("\n\n")

        sb.append("--- تراکنش‌ها (Transactions) ---\n")
        sb.append("شناسه,نوع,مبلغ (تومان),دسته‌بندی,فروشگاه/طرف حساب,شماره کارت,تاریخ,منبع\n")
        for (t in transactions) {
            val typeStr = if (t.type == TransactionType.EXPENSE) "هزینه" else "درآمد"
            val dateStr = dateFormat.format(Date(t.dateMillis))
            val sourceStr = if (t.isParsedFromSms) "پیامک بانکی" else "دستی"
            sb.append("${t.id},\"$typeStr\",${t.amountToman},\"${t.categoryName}\",\"${t.merchantName}\",\"${t.cardNumber ?: ""}\",\"$dateStr\",\"$sourceStr\"\n")
        }
        sb.append("\n")

        sb.append("--- دارایی‌های غیرنقدی (Assets Portfolio) ---\n")
        sb.append("نام دارایی,نوع,مقدار,واحد,قیمت واحد (تومان),ارزش کل (تومان)\n")
        for (a in assets) {
            sb.append("\"${a.name}\",\"${a.type}\",${a.totalQuantity},\"${a.unit}\",${a.currentUnitPriceToman},${a.totalValueToman}\n")
        }
        sb.append("\n")

        return sb.toString()
    }
}
